package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.EquipmentRequest;
import com.leafcutters.antbuildz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequest, Long> {

    @NotNull
    Optional<EquipmentRequest> findById(@NotNull Long Id);

    List<EquipmentRequest> findByBidAcceptedAndRequestExpired(boolean bidAccepted, boolean requestExpired);

    List<EquipmentRequest> findByCustomerAndBidAcceptedAndRequestExpired(User customer, boolean bidAccepted,
            boolean requestExpired);

    List<EquipmentRequest> findByCustomerAndRequestExpired(User customer, boolean requestExpired);

    List<EquipmentRequest> findByCustomerAndBidAccepted(User customer, boolean bidAccepted);

    @Transactional
    @Modifying
    @Query("UPDATE EquipmentRequest e SET e.bidAccepted = TRUE WHERE e.Id = ?1")
    void acceptBid(Long equipmentRequestId);

    @Transactional
    @Modifying
    @Query("UPDATE EquipmentRequest e SET e.requestCompleted = TRUE WHERE e.Id = ?1")
    void completeRequest(Long equipmentRequestId);

    @Transactional
    @Modifying
    @Query("UPDATE EquipmentRequest e SET e.requestExpired = TRUE WHERE e.Id = ?1")
    void expireRequest(Long equipmentRequestId);

    @Query("SELECT CASE WHEN (COUNT(*)>0) THEN TRUE ELSE FALSE END FROM EquipmentRequest er WHERE er.equipmentName = ?1 AND bidAccepted = TRUE")
    boolean findAcceptedExistsByEquipmentName(String equipmentName);

    @Query("SELECT price/requestLength as daily_price FROM EquipmentRequest WHERE bidAccepted = TRUE AND equipmentName = ?1 ORDER by daily_price")
    List<Double> findDailyPriceByEquipmentName(String equipmentName);

}
