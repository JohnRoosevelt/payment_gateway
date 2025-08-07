package com.example.payment.payment_gateway;

public class PaymentResponse {
  private String id;
  private String orderId;
  private String status;
  private String message;

  // Constructors
  public PaymentResponse() {
  }

  public PaymentResponse(String id, String orderId, String status, String message) {
    this.id = id;
    this.orderId = orderId;
    this.status = status;
    this.message = message;
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}