package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u " +
            "SET u.enabled = TRUE WHERE u.email = ?1")
    void enableAppUser(String email);

    @Query("SELECT u from User u WHERE u.appUserRole='PARTNER'")
    List<User> findAllPartners();

    @Query("SELECT u FROM User u WHERE u.name = ?1")
    User findByName(String name);
}
