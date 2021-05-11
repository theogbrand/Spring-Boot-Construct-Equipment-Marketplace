package com.leafcutters.antbuildz.models;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Positive;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "bid")
public class Bid {

    @SequenceGenerator(name = "bid_sequence", sequenceName = "bid_sequence", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bid_sequence")
    @Column(name = "bid_id")
    private Long Id;

    @Positive(message = "Bid price must be a positive number!")
    private Double price;

    private boolean bidRejected = false;
    private boolean bidAccepted = false;
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "equipment_request_id", nullable = false)
    private EquipmentRequest equipmentRequest;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User partner;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment partnerEquipment;

    @Transient
    private Long equipmentRequestId;

    @Transient
    private Long equipmentId;

}
