package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.leafcutters.antbuildz.models.UpgradeRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UpgradeRequestRepository extends JpaRepository<UpgradeRequest, Long> {

    List<UpgradeRequest> findAllByRequestCompleted(boolean requestCompleted);

    List<UpgradeRequest> findAllByUserAndRequestApproved(User customer, boolean requestApproved);

    @Transactional
    @Modifying
    @Query("UPDATE UpgradeRequest u SET u.requestCompleted = TRUE, u.requestApproved = TRUE WHERE u.Id = ?1")
    void acceptRequest(Long requestId);

    @Transactional
    @Modifying
    @Query("UPDATE UpgradeRequest u SET u.requestCompleted = TRUE, u.requestRejected = TRUE WHERE u.Id = ?1")
    void rejectRequest(Long requestId);

}