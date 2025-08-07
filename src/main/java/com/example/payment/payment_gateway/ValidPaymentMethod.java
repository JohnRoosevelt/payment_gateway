package com.example.payment.payment_gateway;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = PaymentMethodValidator.class)
@Documented
public @interface ValidPaymentMethod {
  String message() default "Unsupported payment method. Only 'A' or 'B' are allowed.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}