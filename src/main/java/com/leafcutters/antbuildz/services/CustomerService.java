package com.leafcutters.antbuildz.services;

import com.leafcutters.antbuildz.models.*;

import com.leafcutters.antbuildz.repositories.BidRepository;
import com.leafcutters.antbuildz.repositories.EquipmentRequestRepository;
import com.leafcutters.antbuildz.repositories.PaymentRepository;
import com.leafcutters.antbuildz.repositories.UpgradeRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Service
public class CustomerService {

    @Autowired
    private EquipmentRequestRepository equipmentRequestRepository;
    @Autowired
    private UpgradeRequestRepository upgradeRequestRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private BidRecService bidRecService;
    @Autowired
    private EmailService emailService;
    @Value("${from.email}")
    private String fromEmail;
    @Value("${request.expiry.seconds}")
    private int secondsToExpiry;

    // This function adds an EquipmentRequest to the DB and sends emails to partners
    // that a request has been created
    @Transactional
    public void addEquipmentRequest(EquipmentRequest equipmentRequest, User loggedInUser) {
        equipmentRequest.setCustomer(loggedInUser);
        equipmentRequest.setCreatedAt(LocalDateTime.now());
        equipmentRequest.setExpiresAt(LocalDateTime.now().plusSeconds(secondsToExpiry));
        String equipmentName = equipmentRequest.getEquipmentName();
        double suggestedBid = bidRecService.getBidRecPrice(equipmentName);
        equipmentRequest.setRequestLength(
                Period.between(equipmentRequest.getFromDate(), equipmentRequest.getToDate()).getDays());
        equipmentRequest.setSuggestedBid(suggestedBid * equipmentRequest.getRequestLength());
        equipmentRequestRepository.save(equipmentRequest);
        // Send email to all partners who have that particular equipment available that
        // a request has been created
        emailService.sendNotificationToPartners(equipmentRequest);
    }

    @Transactional
    public void addUpgradeRequest(UpgradeRequest request) {
        upgradeRequestRepository.save(request);
    }

    @Transactional
    public boolean getUpgradeRequest(User customer) {
        List<UpgradeRequest> upgradeRequests = upgradeRequestRepository.findAllByUserAndRequestApproved(customer,
                false);
        if (upgradeRequests.isEmpty()) {
            return true;
        }
        for (UpgradeRequest upgradeRequest : upgradeRequests) {
            if (upgradeRequest.isRequestRejected() && upgradeRequest.isRequestCompleted()) {
                return true;
            } else if (!upgradeRequest.isRequestRejected() && !upgradeRequest.isRequestCompleted()) {
                return false;
            }
        }
        return false;
    }

    // This function checks for expiry of requests and cancels bids where necessary
    @Transactional
    public void expireRequests(User loggedInUser) {
        // Can we run this on application runtime instead of customer launching page
        LocalDateTime dateTimeNow = LocalDateTime.now();
        List<EquipmentRequest> requestsToExpire = equipmentRequestRepository.findByCustomerAndBidAccepted(loggedInUser,
                false);
        for (EquipmentRequest equipmentRequest : requestsToExpire) {
            if (equipmentRequest.getExpiresAt().isBefore(dateTimeNow)) {
                equipmentRequestRepository.expireRequest(equipmentRequest.getId());
                Set<Bid> requestBids = equipmentRequest.getBids();
                for (Bid bid : requestBids) {
                    bid.setBidRejected(true);
                    bid.setRemarks("Request expired");
                }
            }
        }
    }

    // This function gets all EquipmentRequests and their corresponding bids made by
    // Partners for the Customer to view
    @Transactional
    public Map<EquipmentRequest, Set<Bid>> getAllCustomerEquipmentRequestsAndBids(User loggedInUser) {
        Map<EquipmentRequest, Set<Bid>> equipmentRequestBidMap = new HashMap<>();
        List<EquipmentRequest> customerEquipmentRequests = equipmentRequestRepository
                .findByCustomerAndBidAcceptedAndRequestExpired(loggedInUser, false, false);
        for (EquipmentRequest equipmentRequest : customerEquipmentRequests) {
            try {
                equipmentRequestBidMap.put(equipmentRequest,
                        bidRepository.findAllByEquipmentRequestAndBidRejected(equipmentRequest, false));
            } catch (NoSuchElementException e) {
                // Insert error code here to show a blank table
            }
        }
        return equipmentRequestBidMap;
    }

    // This function gets all EquipmentRequests which have expired
    @Transactional
    public List<EquipmentRequest> getAllExpiredEquipmentRequests(User loggedInUser) {
        return equipmentRequestRepository.findByCustomerAndRequestExpired(loggedInUser, true);
    }

    // This function activates when a Customer accepts a bid, rejecting all other
    // bids and creating a new Payment object for that particular Request
    @Transactional
    public void updateEquipmentRequestAndBids(Long bidId, Long equipmentRequestId) {
        equipmentRequestRepository.acceptBid(equipmentRequestId);
        bidRepository.acceptBid(bidId);
        emailService.sendBidAcceptedEmail(bidId, equipmentRequestId);
        // Send email to partner who has won the bid
        bidRepository.rejectBids(bidId, equipmentRequestId);
        // Send emails to partners who have lost the bid

        // Gets the Equipment and EquipmentRequest objects for the successful bid
        Bid bid = bidRepository.findById(bidId).get();
        EquipmentRequest equipmentRequest = equipmentRequestRepository.findById(equipmentRequestId).get();
        Equipment equipment = bid.getPartnerEquipment();

        // Adds booked start and end date to the equipment
        Map<LocalDate, LocalDate> bookedDates = equipment.getBookedDates();
        LocalDate requestStartDate = equipmentRequest.getFromDate();
        LocalDate requestEndDate = equipmentRequest.getToDate();
        bookedDates.put(requestStartDate, requestEndDate);
        equipment.setBookedDates(bookedDates);
        equipment.setEquipmentInUse(true);

        // Cancel the bids where partner has assigned the same piece of Equipment on
        // overlapping dates
        for (Bid cancelBid : equipment.getBids()) {
            Long cancelBidId = cancelBid.getId();
            if (!cancelBidId.equals(bidId)) {

                LocalDate otherRequestStartDate = cancelBid.getEquipmentRequest().getFromDate();
                LocalDate otherRequestEndDate = cancelBid.getEquipmentRequest().getToDate();

                if ((requestStartDate.isBefore(otherRequestEndDate.plusDays(1)))
                        && (requestEndDate.isAfter(otherRequestStartDate.minusDays(1)))) {
                    partnerService.cancelBid(cancelBidId);
                }

            }

        }

        equipmentRequest.setPartnerEquipment(equipment);
        equipmentRequest.setPrice(bid.getPrice());

        Payment payment = new Payment();
        payment.setEquipmentRequest(equipmentRequest);
        payment.setPartner(bid.getPartner());
        payment.setCustomer(equipmentRequest.getCustomer());
        payment.setPrice(bid.getPrice());
        paymentRepository.save(payment);
        // Send email out to customer to make payment (incl. deposit) [i.e. the INVOICE]
    }

    // This function gets all EquipmentRequests that customers have accepted and
    // fetches the corresponding Payment object
    @Transactional
    public Map<EquipmentRequest, Payment> getAcceptedCustomerEquipmentRequests(User loggedInUser) {
        Map<EquipmentRequest, Payment> acceptedEquipmentRequestPaymentMap = new HashMap<>();
        List<EquipmentRequest> customerEquipmentRequests = equipmentRequestRepository
                .findByCustomerAndBidAcceptedAndRequestExpired(loggedInUser, true, false);
        for (EquipmentRequest equipmentRequest : customerEquipmentRequests) {
            acceptedEquipmentRequestPaymentMap.put(equipmentRequest,
                    paymentRepository.findByEquipmentRequest(equipmentRequest).get());
        }
        return acceptedEquipmentRequestPaymentMap;
    }

    // This function indicates that a customer has made the payment for a particular
    // EquipmentRequest
    @Transactional
    public void customerMakePayment(Long paymentId) {
        paymentRepository.customerMakePayment(paymentId);
        // Send email to partner that customer has made payment
    }

    // This function indicates that a customer has completed a particular
    // EquipmentRequest
    @Transactional
    public void customerCompleteEquipmentRequest(Long paymentId) {
        paymentRepository.customerCompleteRequest(paymentId);
        // Send email to customer that they now have to wait for partner to confirm
        // completion before deposit returned
        // Send email to partner to complete request on their end as well
    }

}
