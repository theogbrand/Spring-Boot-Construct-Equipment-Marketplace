package com.leafcutters.antbuildz.controllers;

import com.leafcutters.antbuildz.models.Invoice;
import com.leafcutters.antbuildz.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/home/customer/acceptedRequests/bid/choose")
    public String bidChoose(@RequestParam(value = "paymentId") Long paymentId,
                            Model model) {
        Invoice invoice = invoiceService.createInvoice(paymentId);
        model.addAttribute("invoice",invoice);
        return "customer/invoice";
    }
}
