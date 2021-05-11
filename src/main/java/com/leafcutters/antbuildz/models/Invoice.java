package com.leafcutters.antbuildz.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice implements Serializable {

    @SequenceGenerator(name = "invoice_sequence", sequenceName = "invoice_sequence", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_sequence")
    private Long id;
    private Long paymentId;
    private double cost;

    private String generatedOn;
    private String dueDate;
    @Column(name="user_name")
    private String userName;
    @Column(name="user_email")
    private String userEmail;

    @Column(name="payment_status")
    private String status;

}
