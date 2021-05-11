package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.BidRecommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRecRepository extends JpaRepository<BidRecommendation, Long> {

    @Query("SELECT CASE WHEN (COUNT(*)>0) THEN TRUE ELSE FALSE END FROM BidRecommendation br WHERE br.equipmentName = ?1")
    public boolean existsByEquipmentName(String equipmentName);

    @Query("SELECT recDailyPrice FROM BidRecommendation br WHERE br.equipmentName = ?1")
    public double findPriceByEquipmentName(String equipmentName);
}
