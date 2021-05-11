package com.leafcutters.antbuildz.models;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "upgrade_request")
public class UpgradeRequest {

    @SequenceGenerator(name = "upgrade_request_sequence", sequenceName = "upgrade_request_sequence", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "upgrade_request_sequence")
    @Column(name = "request_id")
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    private boolean requestCompleted = false;
    private boolean requestApproved = false;
    private boolean requestRejected = false;

}
