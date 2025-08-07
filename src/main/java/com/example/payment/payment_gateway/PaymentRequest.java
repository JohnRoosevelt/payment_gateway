package com.example.payment.payment_gateway;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaymentRequest {
  @NotBlank(message = "Order ID is required")
  private String orderId;
  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  private Double amount;
  @NotBlank(message = "Currency is required")
  private String currency;

  @NotBlank(message = "Payment method is required")
  @ValidPaymentMethod // <-- Add the custom annotation here
  private String paymentMethod;

  // Constructors
  public PaymentRequest() {
  }

  public PaymentRequest(String orderId, Double amount, String currency, String paymentMethod) {
    this.orderId = orderId;
    this.amount = amount;
    this.currency = currency;
    this.paymentMethod = paymentMethod;
  }

  // Getters and Setters
  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }
}