package com.example.payment.payment_gateway;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Payment Gateway API", description = "APIs for managing payments")
@RestController
@RequestMapping("/payments")
public class PaymentController {

  @Autowired
  private PaymentService paymentService;

  @Operation(summary = "Create a payment", description = "Initiates a new payment transaction")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Payment created"),
      @ApiResponse(responseCode = "400", description = "Invalid payment request")
  })
  @PostMapping
  public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
    // Validate paymentMethod
    if (!request.isValidPaymentMethod()) {
      return new ResponseEntity<>(new PaymentResponse("", request.getOrderId(), "FAILED",
          "Unsupported payment method. Only 'A' or 'B' are allowed."), HttpStatus.BAD_REQUEST);
    }

    if (request.getAmount() == null || request.getAmount() <= 0 || request.getOrderId() == null) {
      return new ResponseEntity<>(new PaymentResponse("", request.getOrderId(), "FAILED",
          "Invalid payment request: amount must be positive and order ID is required."), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(paymentService.createPayment(request), HttpStatus.OK);
  }

  @Operation(summary = "Approve a payment", description = "Approves a payment with PIN or OTP")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Payment approved"),
      @ApiResponse(responseCode = "400", description = "Invalid PIN/OTP or payment not found")
  })
  @PostMapping("/{id}/approve")
  public PaymentResponse approvePayment(@PathVariable String id, @RequestParam(required = false) String pin,
      @RequestParam(required = false) String otp) {
    return paymentService.approvePayment(id, pin, otp);
  }

  @Operation(summary = "Get payment by ID", description = "Retrieves details of a specific payment")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Payment found"),
      @ApiResponse(responseCode = "404", description = "Payment not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
    Optional<Payment> paymentOptional = paymentService.getPaymentById(id);

    if (paymentOptional.isPresent()) {
      return ResponseEntity.ok(paymentOptional.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(summary = "List all payments", description = "Retrieves a paginated list of payments")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Payments retrieved")
  })
  @GetMapping
  public List<Payment> getAllPayments(@RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return paymentService.getAllPayments(page, size);
  }
}