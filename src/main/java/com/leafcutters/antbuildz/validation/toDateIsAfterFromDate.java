package com.leafcutters.antbuildz.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateValidator.class)
@Documented
public @interface toDateIsAfterFromDate {

    String message() default "To Date cannot be before From Date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

