package com.leafcutters.antbuildz.models;

import com.leafcutters.antbuildz.validation.toDateIsAfterFromDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString
@Table(name = "equipment_request")
@toDateIsAfterFromDate
public class EquipmentRequest {

    @SequenceGenerator(name = "equipment_request_sequence", sequenceName = "equipment_request_sequence", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "equipment_request_sequence")
    @Column(name = "equipment_request_id")
    private Long Id;
    private String equipmentName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "From date cannot be in the past!")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message = "To date cannot be today or in the past!")
    private LocalDate toDate;

    @Nullable
    private Double price;
    private boolean bidAccepted = false;
    private boolean requestCompleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean requestExpired = false;

    private int requestLength;
    private double suggestedBid = 0.0;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User customer;

    @OneToMany(mappedBy = "equipmentRequest")
    private Set<Bid> bids;

    @OneToOne(mappedBy = "equipmentRequest")
    private Payment payment;

    @OneToOne
    @JoinColumn(name = "equipment_id")
    private Equipment partnerEquipment;

}
