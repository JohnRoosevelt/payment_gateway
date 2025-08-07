package com.example.payment.payment_gateway;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentMethodValidator implements ConstraintValidator<ValidPaymentMethod, String> {

  @Override
  public void initialize(ValidPaymentMethod constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String paymentMethod, ConstraintValidatorContext context) {
    if (paymentMethod == null) {
      return false;
    }
    return "A".equalsIgnoreCase(paymentMethod) || "B".equalsIgnoreCase(paymentMethod);
  }
}