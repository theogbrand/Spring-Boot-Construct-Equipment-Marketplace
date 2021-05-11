package com.leafcutters.antbuildz.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @SequenceGenerator(name = "user_sequence", sequenceName = "user_sequence", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequence")
    @Column(name = "user_id")
    private Long Id;
    private String name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role appUserRole;
    private Boolean locked = false;
    private Boolean enabled = false;

    @OneToMany(mappedBy = "customer")
    private Set<EquipmentRequest> equipmentRequests;

    @OneToMany(mappedBy = "partner")
    private Set<Equipment> equipment;

    @OneToMany(mappedBy = "user")
    private Set<UpgradeRequest> upgradeRequest;

    @OneToMany(mappedBy = "partner")
    private Set<Bid> bids;

    @OneToMany(mappedBy = "partner")
    private Set<Payment> incomingPayments;

    @OneToMany(mappedBy = "customer")
    private Set<Payment> outgoingPayments;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(appUserRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
