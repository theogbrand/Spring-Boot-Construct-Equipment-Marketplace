package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.Bid;
import com.leafcutters.antbuildz.models.EquipmentRequest;
import com.leafcutters.antbuildz.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    @NotNull Optional<Bid> findById(@NotNull Long Id);

    Optional<Bid> findByPartnerAndEquipmentRequest(User partner, EquipmentRequest equipmentRequest);

    Set<Bid> findAllByPartner(User partner);

    Set<Bid> findAllByEquipmentRequestAndBidRejected(EquipmentRequest equipmentRequest, boolean bidRejected);

    Set<Bid> findAllByPartnerAndBidAccepted(User partner, boolean bidAccepted);

    void deleteById(@NotNull Long bidId);

    Set<Bid> findByPartnerAndBidRejected(User partner, boolean bidRejected);

    @Transactional
    @Modifying
    @Query("UPDATE Bid b SET b.bidAccepted = TRUE WHERE b.Id = ?1")
    void acceptBid(Long bidId);

    @Transactional
    @Modifying
    @Query("UPDATE Bid b SET b.bidRejected = TRUE, b.remarks = 'Bid rejected' WHERE b.Id <> ?1 AND b.equipmentRequest.Id = ?2")
    void rejectBids(Long bidId, Long equipmentRequestId);

}
