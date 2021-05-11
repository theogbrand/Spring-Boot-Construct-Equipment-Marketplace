package com.leafcutters.antbuildz.services;

import com.leafcutters.antbuildz.models.EquipmentRequest;
import com.leafcutters.antbuildz.models.Invoice;
import com.leafcutters.antbuildz.models.Payment;
import com.leafcutters.antbuildz.repositories.InvoiceRepository;
import com.leafcutters.antbuildz.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    public Invoice createInvoice(Long paymentId) {

        Payment payment = null;
        if (paymentRepository.findById(paymentId).isPresent()) {
            payment = paymentRepository.findById(paymentId).get();
        }

        assert payment != null;
        EquipmentRequest equipmentRequest = payment.getEquipmentRequest();

        Invoice invoice = new Invoice();
        invoice.setPaymentId(paymentId);
        invoice.setCost(payment.getPrice());
        invoice.setStatus("P");
        invoice.setUserName(equipmentRequest.getCustomer().getName());
        invoice.setUserEmail(equipmentRequest.getCustomer().getEmail());

        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String startDate = dateFormat.format(date);

        // 3 days to accept
        cal.add(Calendar.DATE, +3);
        Date currDatePlus3 = cal.getTime();
        String dueDate = dateFormat.format(currDatePlus3);

        invoice.setGeneratedOn(startDate);
        invoice.setDueDate(dueDate);
        invoiceRepository.save(invoice);
        return invoice;
    }

}
