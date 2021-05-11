package com.leafcutters.antbuildz.controllers;

import com.leafcutters.antbuildz.models.*;
import com.leafcutters.antbuildz.services.BidRecService;
import com.leafcutters.antbuildz.services.PartnerService;
import com.leafcutters.antbuildz.services.PartnerService.BidEquipmentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class PartnerController {

    @Autowired
    private PartnerService partnerService;
    @Autowired
    private BidRecService bidRecService;

    @ModelAttribute
    public void addAttributes(Model model) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Long> idOfRequestsWithBid = partnerService.getEquipmentRequestsWithAlreadySubmittedBidIds(loggedInUser);
        Map<EquipmentRequest, BidEquipmentWrapper> mapOfOpenRequestsAndBidsAndAvailableEquipments = partnerService.getAlOpenEquipmentRequestsAndBidsAndAvailableEquipments(loggedInUser);
        Map<EquipmentRequest, Bid> mapOfExpiredAndRejectedRequestsWithBid = partnerService.getAllRejectedEquipmentRequestsAndBids(loggedInUser);
        Map<Equipment, Map<LocalDate, LocalDate>> equipmentAndBookedDatesMap = partnerService.getAllEquipmentAndBookedDates(loggedInUser);
        List<BidRecommendation> bidRecommendations = bidRecService.findAll();
        model.addAttribute("idOfRequestsWithBid", idOfRequestsWithBid);
        model.addAttribute("mapOfOpenRequestsAndBidsAndAvailableEquipments", mapOfOpenRequestsAndBidsAndAvailableEquipments);
        model.addAttribute("mapOfExpiredRequestsWithBid", mapOfExpiredAndRejectedRequestsWithBid);
        model.addAttribute("bidRecommendations", bidRecommendations);
        model.addAttribute("equipmentAndBookedDatesMap", equipmentAndBookedDatesMap);
    }

    @GetMapping("/home/partner")
    public String partnerView(Bid bid, Model model) {
        model.addAttribute("bid", bid);
        return "partner/partner";
    }

    @PostMapping("/home/partner/createBid")
    public String partnerCreateBid(@Valid @ModelAttribute("bid") Bid bid, BindingResult result, Model model) {
        model.addAttribute("bid", bid);

        if (result.hasErrors()) {
            return "partner/partner";
        }

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        partnerService.createBid(bid, loggedInUser);
        return "redirect:/home/partner";
    }

    @GetMapping("/home/partner/cancelBid")
    public String partnerCancelBid(@RequestParam(name = "bidId") Long bidId) {
        partnerService.cancelBid(bidId);
        return "redirect:/home/partner";
    }

    @GetMapping("/home/partner/acceptedBids")
    public String partnerAcceptedBidsView(Model model) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<EquipmentRequest, Payment> acceptedEquipmentRequestsWithPayment = partnerService
                .getEquipmentRequestsWithAcceptedBid(loggedInUser);
        model.addAttribute("acceptedEquipmentRequestsWithPayment", acceptedEquipmentRequestsWithPayment);
        return "partner/partner-successful-bids";
    }

    @GetMapping("/home/partner/acceptedBids/completeRequest")
    public String partnerCompletedRequestView(@RequestParam(name = "paymentId") Long paymentId,
            @RequestParam(name = "equipmentRequestId") Long equipmentRequestId) {
        partnerService.partnerCompleteEquipmentRequest(equipmentRequestId, paymentId);
        return "redirect:/home/partner/acceptedBids";
    }

    @GetMapping("/home/partner/addEquipment")
    public String partnerAddEquipmentView(Equipment equipment, Model model) {
        model.addAttribute("equipment", equipment);
        return "partner/add-equipment";
    }

    @PostMapping("/home/partner/addEquipment/submit")
    public String partnerAddEquipment(@Valid @ModelAttribute("equipment") Equipment equipment, BindingResult result, Model model) {
        model.addAttribute("equipment", equipment);

        if (result.hasErrors()) {
            return "partner/add-equipment";
        }

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        partnerService.addEquipment(equipment, loggedInUser);
        return "redirect:/home/partner";
    }

}
