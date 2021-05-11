package com.leafcutters.antbuildz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import com.leafcutters.antbuildz.models.BidRecommendation;
import com.leafcutters.antbuildz.models.UpgradeRequest;
import com.leafcutters.antbuildz.services.AdminService;
import com.leafcutters.antbuildz.services.BidRecService;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private BidRecService bidRecService;

    @GetMapping("/home/admin")
    public String adminView(Model theModel) {
        List<BidRecommendation> bidRecommendations = bidRecService.findAll();
        List<UpgradeRequest> upgradeRequests = adminService.findAllIncompleteRequests();
        theModel.addAttribute("upgradeRequests", upgradeRequests);
        theModel.addAttribute("bidrec", bidRecommendations);
        return "admin/admin";
    }

    @GetMapping("/home/admin/rejectUpgradeRequest")
    public String rejectUpgradeRequest(@RequestParam("requestId") Long requestId) {
        // rejects the User's UpgradeRequest
        adminService.rejectUpgradeRequest(requestId);
        // redirect
        return "redirect:/home/admin";
    }

    @GetMapping("/home/admin/acceptUpgradeRequest")
    public String acceptUpgradeRequest(@RequestParam("requestId") Long requestId,
                                       @RequestParam("customerId") Long userId) {
        // accepts the User's UpgradeRequest
        adminService.acceptUpgradeRequest(requestId);
        // edit Unlock user and change to partner details
        adminService.unlockUser(userId);
        adminService.customerToPartnerUpdate(userId);
        // redirect
        return "redirect:/home/admin";
    }

}