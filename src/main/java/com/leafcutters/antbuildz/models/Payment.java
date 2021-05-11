package com.leafcutters.antbuildz.models;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "payment")
public class Payment {

    @SequenceGenerator(name = "payment_sequence", sequenceName = "payment_sequence", allocationSize = 1)
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_sequence")
    @Column(name = "payment_id")
    private Long Id;
    @NonNull
    private Double price;
    private boolean customerPaymentMade = false;
    private boolean customerCompletedRequest = false;
    private boolean partnerCompletedRequest = false;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "partner_id", referencedColumnName = "user_id", nullable = false)
    private User partner;

    @OneToOne
    @JoinColumn(name = "equipment_request_id", nullable = false)
    private EquipmentRequest equipmentRequest;

}
