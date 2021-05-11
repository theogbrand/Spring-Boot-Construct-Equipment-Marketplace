package com.leafcutters.antbuildz.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Positive;

import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "bid_recommendation")

public class BidRecommendation {
    @SequenceGenerator(name = "bid_rec_sequence", sequenceName = "bid_rec_sequence", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bid_rec_sequence")
    @Column(name = "bid_recommendation_id")
    private Long recommendationId;

    private String equipmentName;
    private double systemRecPrice = 0.0;

    @Positive(message = "Price should be larger than zero")
    private double recDailyPrice;

}
