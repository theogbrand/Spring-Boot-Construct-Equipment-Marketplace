package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.EquipmentRequest;
import com.leafcutters.antbuildz.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
// import java.util.Set;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByEquipmentRequest(EquipmentRequest equipmentRequest);

    @Transactional
    @Modifying
    @Query("UPDATE Payment p SET p.customerPaymentMade = TRUE WHERE p.Id = ?1")
    void customerMakePayment(Long paymentId);

    @Transactional
    @Modifying
    @Query("UPDATE Payment p SET p.customerCompletedRequest = TRUE WHERE p.Id = ?1")
    void customerCompleteRequest(Long paymentId);

    @Transactional
    @Modifying
    @Query("UPDATE Payment p SET p.partnerCompletedRequest = TRUE WHERE p.Id = ?1")
    void partnerCompleteRequest(Long paymentId);

}
