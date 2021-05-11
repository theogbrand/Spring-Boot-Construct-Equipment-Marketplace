package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.Equipment;
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
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @NotNull Optional<Equipment> findById(@NotNull Long equipmentId);

    List<Equipment> findByPartner(User loggedInUser);

    @Transactional
    @Modifying
    @Query("UPDATE Equipment e SET e.equipmentInUse = TRUE WHERE e.Id = ?1")
    void equipmentInUse(Long equipmentId);

    @Transactional
    @Modifying
    @Query("UPDATE Equipment e SET e.equipmentInUse = FALSE WHERE e.Id = ?1")
    void equipmentNotInUse(Long equipmentId);

}
