package com.leafcutters.antbuildz.services;

import com.leafcutters.antbuildz.models.*;
import com.leafcutters.antbuildz.repositories.BidRepository;
import com.leafcutters.antbuildz.repositories.EquipmentRepository;
import com.leafcutters.antbuildz.repositories.EquipmentRequestRepository;
import com.leafcutters.antbuildz.repositories.PaymentRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class PartnerService {

    @Autowired
    private EquipmentRequestRepository equipmentRequestRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;
    @Autowired
    private EmailService emailService;
    @Value("${from.email}")
    private String fromEmail;

    @Transactional
    public void createBid(Bid bid, User loggedInUser) {
        bid.setPartner(loggedInUser);
        EquipmentRequest equipmentRequest = equipmentRequestRepository.findById(bid.getEquipmentRequestId()).get();
        Equipment equipment = equipmentRepository.findById(bid.getEquipmentId()).get();
        bid.setEquipmentRequest(equipmentRequest);
        bid.setPartnerEquipment(equipment);
        bidRepository.save(bid);
        // Insert code to send email update to customer that a bid has been submitted
        emailService.sendNotificationToCustomer(equipmentRequest);
    }

    @Transactional
    public void cancelBid(Long bidId) {
        // Bid bid = bidRepository.findById(bidId).get(); // In case we want to use bid
        // object that is being cancelled
        bidRepository.deleteById(bidId);
        // Insert code to send email update to customer that a bid has been cancelled
    }

    // Adds a piece of Equipment to a particular partner's inventory
    @Transactional
    public void addEquipment(Equipment equipment, User loggedInUser) {
        equipment.setPartner(loggedInUser);
        equipmentRepository.save(equipment);
    }

    // Retrieves all Equipment a partner has listed and their booked dates
    @Transactional
    public Map<Equipment, Map<LocalDate, LocalDate>> getAllEquipmentAndBookedDates(User loggedInUser) {
        Map<Equipment, Map<LocalDate, LocalDate>> equipmentAndBookedDatesMap = new HashMap<>();
        for (Equipment equipment : equipmentRepository.findByPartner(loggedInUser)) {
            equipmentAndBookedDatesMap.put(equipment, equipment.getBookedDates());
        }
        return  equipmentAndBookedDatesMap;
    }

    @Transactional
    public List<Long> getEquipmentRequestsWithAlreadySubmittedBidIds(User loggedInUser) {
        Set<Bid> userBids = bidRepository.findAllByPartner(loggedInUser);
        List<Long> idOfRequestsWithBid = new ArrayList<>();
        for (Bid bid : userBids) {
            idOfRequestsWithBid.add(bid.getEquipmentRequest().getId());
        }
        return idOfRequestsWithBid;
    }

    @Transactional
    public Map<EquipmentRequest, BidEquipmentWrapper> getAlOpenEquipmentRequestsAndBidsAndAvailableEquipments(
            User loggedInUser) {

        Map<EquipmentRequest, BidEquipmentWrapper> mapOfOpenRequestsAndBidsAndAvailableEquipments = new HashMap<>();
        List<EquipmentRequest> openEquipmentRequests = equipmentRequestRepository
                .findByBidAcceptedAndRequestExpired(false, false);

        for (EquipmentRequest equipmentRequest : openEquipmentRequests) {

            LocalDate requestStartDate = equipmentRequest.getFromDate();
            LocalDate requestEndDate = equipmentRequest.getToDate();

            Set<String> availableEquipmentTypes = partnerAvailableEquipment(loggedInUser);

            if (availableEquipmentTypes.contains(equipmentRequest.getEquipmentName())) {

                mapOfOpenRequestsAndBidsAndAvailableEquipments.put(equipmentRequest, new BidEquipmentWrapper());

                // Adds bids already made for a particular request. Null object if not made
                if (bidRepository.findByPartnerAndEquipmentRequest(loggedInUser, equipmentRequest).isPresent()) {
                    Bid bid = bidRepository.findByPartnerAndEquipmentRequest(loggedInUser, equipmentRequest).get();
                    BidEquipmentWrapper tempWrapper = mapOfOpenRequestsAndBidsAndAvailableEquipments
                            .get(equipmentRequest);
                    tempWrapper.setBid(bid);
                    mapOfOpenRequestsAndBidsAndAvailableEquipments.put(equipmentRequest, tempWrapper);
                } else {
                    BidEquipmentWrapper tempWrapper = mapOfOpenRequestsAndBidsAndAvailableEquipments
                            .get(equipmentRequest);
                    tempWrapper.setBid(null);
                    mapOfOpenRequestsAndBidsAndAvailableEquipments.put(equipmentRequest, tempWrapper);
                }

                // Adds equipment which is available to be used for this request, based on dates
                ArrayList<Equipment> compatibleEquipment = new ArrayList<>();
                List<Equipment> availableEquipment = compatiblePartnerEquipment(loggedInUser, requestStartDate,
                        requestEndDate);
                for (Equipment equipment : availableEquipment) {
                    if (equipment.getEquipmentName().equals(equipmentRequest.getEquipmentName())) {
                        compatibleEquipment.add(equipment);
                    }
                }

                if (compatibleEquipment.isEmpty()) {
                    mapOfOpenRequestsAndBidsAndAvailableEquipments.remove(equipmentRequest);
                } else {
                    // Saving the compatible equipment to the Map
                    BidEquipmentWrapper tempWrapper = mapOfOpenRequestsAndBidsAndAvailableEquipments.get(equipmentRequest);
                    tempWrapper.setEquipmentList(compatibleEquipment);
                    mapOfOpenRequestsAndBidsAndAvailableEquipments.put(equipmentRequest, tempWrapper);
                }

            }

        }
        return mapOfOpenRequestsAndBidsAndAvailableEquipments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BidEquipmentWrapper {
        private Bid bid;
        private ArrayList<Equipment> equipmentList;

    }

    @Transactional
    public Map<EquipmentRequest, Bid> getAllRejectedEquipmentRequestsAndBids(User loggedInUser) {
        Map<EquipmentRequest, Bid> mapOfRejectedAndExpiredRequestsWithBid = new HashMap<>();
        Set<Bid> rejectedBids = bidRepository.findByPartnerAndBidRejected(loggedInUser, true);
        for (Bid bid : rejectedBids) {
            mapOfRejectedAndExpiredRequestsWithBid.put(bid.getEquipmentRequest(), bid);
        }
        return mapOfRejectedAndExpiredRequestsWithBid;
    }

    @Transactional
    public Map<EquipmentRequest, Payment> getEquipmentRequestsWithAcceptedBid(User loggedInUser) {
        Map<EquipmentRequest, Payment> equipmentRequestsWithAcceptedBid = new HashMap<>();
        Set<Bid> userBids = bidRepository.findAllByPartnerAndBidAccepted(loggedInUser, true);
        for (Bid bid : userBids) {
            equipmentRequestsWithAcceptedBid.put(bid.getEquipmentRequest(), bid.getEquipmentRequest().getPayment());
        }
        return equipmentRequestsWithAcceptedBid;
    }

    @Transactional
    public void partnerCompleteEquipmentRequest(Long equipmentRequestId, Long paymentId) {
        equipmentRequestRepository.completeRequest(equipmentRequestId);
        paymentRepository.partnerCompleteRequest(paymentId);
        if (equipmentRequestRepository.findById(equipmentRequestId).isPresent()) {
            equipmentRepository.equipmentNotInUse(
                    equipmentRequestRepository.findById(equipmentRequestId).get().getPartnerEquipment().getId());
        }

    }

    // Function to determine what is the available equipment a partner has
    @Transactional
    public Set<String> partnerAvailableEquipment(User loggedInUser) {
        List<Equipment> availableEquipment = equipmentRepository.findByPartner(loggedInUser);
        Set<String> availableEquipmentTypes = new HashSet<>();
        for (Equipment equipment : availableEquipment) {
            availableEquipmentTypes.add(equipment.getEquipmentName());
        }
        return availableEquipmentTypes;
    }

    @Transactional
    public List<Equipment> compatiblePartnerEquipment(User loggedInUser, LocalDate requestStartDate,
            LocalDate requestEndDate) {
        List<Equipment> availableEquipment = equipmentRepository.findByPartner(loggedInUser);
        List<Equipment> compatibleEquipment = new ArrayList<>();

        for (Equipment equipment : availableEquipment) {
            Map<LocalDate, LocalDate> equipmentBookedDates = equipment.getBookedDates();
            if (equipmentBookedDates == null) {
                compatibleEquipment.add(equipment);
            } else if (!(dateOverlaps(equipmentBookedDates, requestStartDate, requestEndDate))) {
                compatibleEquipment.add(equipment);
            }
        }
        return compatibleEquipment;
    }

    public boolean dateOverlaps(Map<LocalDate, LocalDate> mapOfDates, LocalDate startDate, LocalDate endDate) {
        for (Map.Entry<LocalDate, LocalDate> booking : mapOfDates.entrySet()) {
            LocalDate bookingStart = booking.getKey();
            LocalDate bookingEnd = booking.getValue();
            if ((bookingStart.isBefore(endDate.plusDays(1))) && (bookingEnd.isAfter(startDate.minusDays(1)))) {
                return true;
            }
        }
        return false;
    }

}
