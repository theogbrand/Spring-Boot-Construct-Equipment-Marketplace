package com.leafcutters.antbuildz.services;

import com.leafcutters.antbuildz.repositories.BidRecRepository;

import java.util.List;
import java.util.Optional;

import com.leafcutters.antbuildz.models.BidRecommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidRecService {
    @Autowired
    private BidRecRepository bidRecRepository;

    public List<BidRecommendation> findAll() {
        return bidRecRepository.findAll();
    }

    public Optional<BidRecommendation> findById(Long theId) {
        return bidRecRepository.findById(theId);
    }

    public void save(BidRecommendation br) {
        bidRecRepository.save(br);
    }

    public void deletebyId(Long theId) {
        bidRecRepository.deleteById(theId);
    }

    public boolean existsByEquipmentName(String equipmentName) {
        return bidRecRepository.existsByEquipmentName(equipmentName);
    }

    public double getBidRecPrice(String equipmentName) {
        if (existsByEquipmentName(equipmentName)) {
            return bidRecRepository.findPriceByEquipmentName(equipmentName);
        }
        return 0.0;
    }

}