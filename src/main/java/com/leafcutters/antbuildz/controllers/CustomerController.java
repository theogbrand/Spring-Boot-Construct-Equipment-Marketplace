package com.leafcutters.antbuildz.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.leafcutters.antbuildz.models.*;

import com.leafcutters.antbuildz.models.Bid;
import com.leafcutters.antbuildz.models.EquipmentRequest;
import com.leafcutters.antbuildz.models.User;
import com.leafcutters.antbuildz.models.UpgradeRequest;

import com.leafcutters.antbuildz.services.CustomerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Homepage for customer. Can view all active EquipmentRequests and their submitted bids for selection
    @GetMapping("/home/customer")
    public String customerView(Model model) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerService.expireRequests(loggedInUser); // Consider running this on a scheduled basis
        Map<EquipmentRequest, Set<Bid>> customerEquipmentRequestsAndBids = customerService.getAllCustomerEquipmentRequestsAndBids(loggedInUser);
        boolean noActiveUpgradeRequest = customerService.getUpgradeRequest(loggedInUser);
        List<EquipmentRequest> expiredRequests = customerService.getAllExpiredEquipmentRequests(loggedInUser);
        model.addAttribute("customerEquipmentRequestsAndBids", customerEquipmentRequestsAndBids);
        model.addAttribute("noActiveUpgradeRequest", noActiveUpgradeRequest);
        model.addAttribute("expiredRequests", expiredRequests);
        return "customer/customer";
    }

    // Page for Customer to create a new EquipmentRequest
    @GetMapping("/home/customer/create")
    public String customerRequestSubmitView(EquipmentRequest equipmentRequest, Model model) {
        model.addAttribute("equipmentRequest", equipmentRequest);
        return "customer/create_request";
    }

    // Actually sends the new EquipmentRequest to the DB
    @PostMapping("/home/customer/create/newRequest")
    public String customerRequestSubmit(@Valid @ModelAttribute("equipmentRequest") EquipmentRequest equipmentRequest, BindingResult result, Model model) {
        model.addAttribute("equipmentRequest", equipmentRequest);

        if (result.hasErrors()) {
            return "customer/create_request";
        }

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerService.addEquipmentRequest(equipmentRequest, loggedInUser);
        return "redirect:/home/customer";
    }

    // Endpoint for accepting a bid for a particular EquipmentRequest. Go to home/customer/acceptedRequests to view the accepted request
    @GetMapping("/home/customer/acceptBid")
    public String customerAcceptBidView(@RequestParam(name = "bidId") Long bidId,
                                        @RequestParam(name = "equipmentRequestId") Long equipmentRequestId) {
        customerService.updateEquipmentRequestAndBids(bidId, equipmentRequestId);
        return "redirect:/home/customer";
    }

    // Customer can view all accepted requests here, make payment and indicate completion
    @GetMapping("/home/customer/acceptedRequests")
    public String customerAcceptedRequestsView(Model model) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<EquipmentRequest, Payment> acceptedEquipmentRequestPayments = customerService.getAcceptedCustomerEquipmentRequests(loggedInUser);
        model.addAttribute("acceptedEquipmentRequestPayments", acceptedEquipmentRequestPayments);
        return "customer/customer-accepted-requests";
    }

    @PostMapping("/home/customer/acceptedRequests/makePayment")
    public String customerMadePaymentView(@RequestParam(name = "paymentId") Long paymentId) {
        customerService.customerMakePayment(paymentId);
        return "redirect:/home/customer/acceptedRequests";
    }

    // Endpoint to indicate Customer-side completion of a particular EquipmentRequest
    @GetMapping("/home/customer/acceptedRequests/completeRequest")
    public String customerCompletedRequestView(@RequestParam(name = "paymentId") Long paymentId) {
        customerService.customerCompleteEquipmentRequest(paymentId);
        return "redirect:/home/customer/acceptedRequests";
    }

    // Endpoint to submit request for upgrade from customer to partner
    @GetMapping("/home/customer/upgrade-request")
    public String customerUpgradeRequest() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UpgradeRequest upgradeRequest = new UpgradeRequest();
        upgradeRequest.setUser(loggedInUser);
        customerService.addUpgradeRequest(upgradeRequest);
        return "customer/partner-registration";
    }

}
