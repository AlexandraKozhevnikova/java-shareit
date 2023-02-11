package ru.practicum.booking;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Constraint(validatedBy = DateConsistencyValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface DateConsistency {
    String message() default "start time must be before end time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}