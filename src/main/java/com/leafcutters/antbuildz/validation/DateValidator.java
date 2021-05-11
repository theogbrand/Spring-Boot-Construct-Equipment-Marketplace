package com.leafcutters.antbuildz.validation;

import com.leafcutters.antbuildz.models.EquipmentRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<toDateIsAfterFromDate, EquipmentRequest> {

    @Override
    public void initialize(toDateIsAfterFromDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(EquipmentRequest equipmentRequest, ConstraintValidatorContext constraintValidatorContext) {
        LocalDate fromDate = equipmentRequest.getFromDate();
        LocalDate toDate = equipmentRequest.getToDate();
        return toDate.isAfter(fromDate);
    }
}
