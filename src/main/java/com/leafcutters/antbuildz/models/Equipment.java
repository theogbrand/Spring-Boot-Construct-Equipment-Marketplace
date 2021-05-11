package com.leafcutters.antbuildz.models;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString
@Table(name = "equipment")
public class Equipment {

    @SequenceGenerator(name = "equipment_sequence", sequenceName = "equipment_sequence", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "equipment_sequence")
    @Column(name = "equipment_id")
    private Long Id;
    private String equipmentName;
    private boolean equipmentInUse = false;
    @NotEmpty(message = "Model cannot be empty")
    private String model;
    @Positive(message = "Manufactured year must be a positive number")
    private int manufactureYear;
    @NotEmpty(message = "Description cannot be empty")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "equipment_booked_dates_map",
            joinColumns = {@JoinColumn(name = "equipment_id", referencedColumnName = "equipment_id")})
    @MapKeyColumn(name = "start_date")
    @Column(name = "end_date")
    private Map<LocalDate, LocalDate> bookedDates;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User partner;

    @OneToMany(mappedBy = "partnerEquipment")
    private Set<Bid> bids;

}
