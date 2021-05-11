package com.leafcutters.antbuildz.services;

import java.util.List;

// import com.leafcutters.antbuildz.models.EquipmentRequest;
import com.leafcutters.antbuildz.models.Role;
import com.leafcutters.antbuildz.models.UpgradeRequest;
import com.leafcutters.antbuildz.models.User;
import com.leafcutters.antbuildz.repositories.EquipmentRequestRepository;
import com.leafcutters.antbuildz.repositories.UpgradeRequestRepository;
import com.leafcutters.antbuildz.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private UpgradeRequestRepository upgradeRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EquipmentRequestRepository equipmentRequestRepository;

    public List<UpgradeRequest> findAllIncompleteRequests() {
        return upgradeRequestRepository.findAllByRequestCompleted(false);
    }

    public void acceptUpgradeRequest(Long requestId) {
        upgradeRequestRepository.acceptRequest(requestId);
    }

    public void rejectUpgradeRequest(Long requestId) {
        upgradeRequestRepository.rejectRequest(requestId);
    }

    public void customerToPartnerUpdate(Long customerId) {
        if (userRepository.findById(customerId).isPresent()) {
            User user = userRepository.findById(customerId).get();
            user.setAppUserRole(Role.PARTNER);
            userRepository.save(user);
        }
    }

    public void unlockUser(Long partnerId) {
        if (userRepository.findById(partnerId).isPresent()) {
            User user = userRepository.findById(partnerId).get();
            user.setLocked(false);
            userRepository.save(user);
        }
    }

    public boolean requestExists(String equipmentName) {
        return equipmentRequestRepository.findAcceptedExistsByEquipmentName(equipmentName);
    }

    public double getMedianBid(String equipmentName) {
        if (!requestExists(equipmentName)){
            return 0.0;
        }
        List<Double> sortedBids = equipmentRequestRepository.findDailyPriceByEquipmentName(equipmentName);
        int numBids = sortedBids.size();
        int medianIndex;
        if (numBids % 2 != 0) {
            medianIndex = Math.floorDiv(numBids, 2);
        } else {
            medianIndex = Math.floorDiv(numBids, 2) - 1;
        }
        if (sortedBids.get(medianIndex) == null) {
            return 0.0;
        }
        return sortedBids.get(medianIndex);
    }
}
