package com.leafcutters.antbuildz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.leafcutters.antbuildz.models.BidRecommendation;
import com.leafcutters.antbuildz.services.AdminService;
import com.leafcutters.antbuildz.services.BidRecService;

import javax.validation.Valid;

@Controller
public class BidRecController {

    @Autowired
    private BidRecService bidRecService;
    @Autowired
    private AdminService adminService;

    @PostMapping("/home/admin/bid/save")
    public String saveBid(@Valid @ModelAttribute("bidrec") BidRecommendation bidrec, BindingResult result, Model model) {
        model.addAttribute("bidrec", bidrec);

        if (result.hasErrors()) {
            return "admin/bid_recommendation_new";
        }

        // save the bid only if it is a new bid recommendation, and recommendation for
        // that equipment does not exist
        if (bidrec.getRecommendationId() == null && bidRecService.existsByEquipmentName(bidrec.getEquipmentName())) {
            return "redirect:/home/admin/recommendation-unsuccessful";
        }
        bidRecService.save(bidrec);
        return "redirect:/home/admin";
    }

    @PostMapping("/home/admin/bid/saveUpdate")
    public String saveBidUpdate(@Valid @ModelAttribute("bidrec") BidRecommendation bidrec, BindingResult result, Model model) {
        model.addAttribute("bidrec", bidrec);

        if (result.hasErrors()) {
            return "admin/bid_recommendation_update";
        }

        // save the bid only if it is a new bid recommendation, and recommendation for
        // that equipment does not exist
        if (bidrec.getRecommendationId() == null && bidRecService.existsByEquipmentName(bidrec.getEquipmentName())) {
            return "redirect:/home/admin/recommendation-unsuccessful";
        }
        bidRecService.save(bidrec);
        return "redirect:/home/admin";
    }

    @GetMapping("/home/admin/recommendation-unsuccessful")
    public String failedRecommendation() {
        return "admin/recommendation-unsuccessful";
    }

    @GetMapping("/home/admin/bid-rec-form/update")
    public String updateBidRec(@RequestParam("recId") Long theId, Model theModel) {
        // Getting the bid recommendation for the given ID
        BidRecommendation bidRecommendation = bidRecService.findById(theId).get();
        // Set bidrec as a model attribute to pre-populate the form
        theModel.addAttribute("bidrec", bidRecommendation);
        // send over the form
        return "admin/bid_recommendation_update";
    }

    @GetMapping("/home/admin/bid-rec-form")
    public String newBidRec(Model theModel) {
        // Creating new bid recommendation object
        BidRecommendation bidRecommendation = new BidRecommendation();
        // Set bidrec as a model attribute
        theModel.addAttribute("bidrec", bidRecommendation);
        // send over the form
        return "admin/bid_recommendation_new";
    }

    @GetMapping("/home/admin/bid-rec-form/delete")
    public String deletebidRec(@RequestParam("recId") Long theId) {
        // delete recommendation by id
        bidRecService.deletebyId(theId);
        return "redirect:/home/admin";
    }

    @GetMapping("/home/admin/bid-rec-form/systemupdate")
    public String updateSysRec(@RequestParam("equipmentName") String equipmentName, @RequestParam("recId") Long theId) {
        BidRecommendation br = bidRecService.findById(theId).get();
        double systemRecPrice = adminService.getMedianBid(equipmentName);
        br.setSystemRecPrice(systemRecPrice);
        bidRecService.save(br);
        return "redirect:/home/admin";
    }

}