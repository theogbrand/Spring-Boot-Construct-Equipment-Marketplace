package com.leafcutters.antbuildz.services;

import com.leafcutters.antbuildz.models.EquipmentRequest;
import com.leafcutters.antbuildz.models.Role;
import com.leafcutters.antbuildz.models.User;
import com.leafcutters.antbuildz.repositories.BidRepository;
import com.leafcutters.antbuildz.repositories.EquipmentRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private EquipmentRequestRepository equipmentRequestRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${from.email}")
    private String fromEmail;
    @Value("${request.expiry.seconds}")
    private int secondsToExpiry;

    @Async
    public void send(String toEmail, String emailHTML, Role role) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailHTML, true);
            helper.setTo(toEmail);
            helper.setSubject(String.format("Confirm your email as a %s", role.toString()));
            helper.setFrom("noreply@antbuildz.com");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email");
        }
    }

    public void sendNotificationToPartners(EquipmentRequest equipmentRequest) {
        try {

            ArrayList<User> partners = (ArrayList<User>) userService.getPartnerList();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            String fromAddress = fromEmail;
            String senderName = "Leafcutter Ants";
            String subject = "A customer has submitted a new Equipment Request!";
            String content = "Dear [[name]],<br>"
                    + "A new request for equipment rental has been created! Login to check it out! =)";
            helper.setFrom(fromAddress, senderName);
            helper.setSubject(subject);

            for (User partner : partners) {

                Set<String> availableEquipmentTypes = partnerService.partnerAvailableEquipment(partner);

                if (availableEquipmentTypes.contains(equipmentRequest.getEquipmentName())) {

                    String toAddress = partner.getEmail();
                    helper.setTo(toAddress);

                    content = content.replace("[[name]]", partner.getName());

                    helper.setText(content, true);
                    mailSender.send(message);
                }

            }

        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }

    }

    public void sendNotificationToCustomer(EquipmentRequest equipmentRequest) {
        try {

            User user = equipmentRequest.getCustomer();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            String fromAddress = fromEmail;
            String senderName = "Leafcutter Ants";
            String subject = "A partner has just responded to your Equipment Request!";
            String content = "Dear [[name]],<br>"
                    + "A new bid for your equipment request has been created! Login to check it out! =)";
            helper.setFrom(fromAddress, senderName);
            helper.setSubject(subject);

            String toAddress = user.getEmail();
            helper.setTo(toAddress);

            content = content.replace("[[name]]", user.getName());

            helper.setText(content, true);
            mailSender.send(message);

        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendBidAcceptedEmail(Long bidId, Long equipmentRequestId) {

        User partner = bidRepository.findById(bidId).get().getPartner();
        EquipmentRequest request = equipmentRequestRepository.findById(equipmentRequestId).get();
        String equipmentName = request.getEquipmentName();
        String duration = request.getFromDate().toString() + " - " + request.getToDate().toString();

        String email = partner.getEmail();
        String name = partner.getName();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            String fromAddress = fromEmail;
            String senderName = "Leafcutter Ants";
            String subject = "A customer has just accepted your bid!";
            String content = String.format(
                    "Dear %s,<br>\n\tSomeone has accepted your bid for %s from %s! You will receive payment shortly",
                    name, equipmentName, duration);
            helper.setFrom(fromAddress, senderName);
            helper.setSubject(subject);

            helper.setTo(email);
            helper.setText(content, true);
            mailSender.send(message);

        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }

    }
}