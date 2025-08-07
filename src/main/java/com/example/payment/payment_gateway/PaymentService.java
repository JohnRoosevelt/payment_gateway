package com.example.payment.payment_gateway;

import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
  private final PaymentRepository paymentRepository;

  public PaymentService(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  public PaymentResponse createPayment(PaymentRequest request) {
    Payment payment = new Payment(request.getOrderId(), request.getAmount(), request.getCurrency(),
        request.getPaymentMethod(), "PENDING");
    paymentRepository.save(payment);

    return new PaymentResponse(payment.getId(), request.getOrderId(), "PENDING", "Payment created");
  }

  @Transactional
  public PaymentResponse approvePayment(String id, String pin, String otp) {
    Optional<Payment> paymentOpt = paymentRepository.findById(id);

    if (paymentOpt.isPresent()) {
      Payment payment = paymentOpt.get();

      if (!"PENDING".equals(payment.getStatus())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is not in PENDING status.");
      }

      String paymentMethod = payment.getPaymentMethod();
      boolean isValid = false;

      if ("A".equals(paymentMethod)) {
        if ("123456".equals(pin)) {
          isValid = true;
        } else {
          payment.setStatus("FAILED");
          paymentRepository.save(payment);
          return new PaymentResponse(payment.getId(), payment.getOrderId(), "FAILED", "Invalid PIN");
        }
      } else if ("B".equals(paymentMethod)) {
        final int maxRetries = 3;
        int retries = 0;

        while (retries < maxRetries) {
          try {
            Payment latestPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found."));

            if ("999999".equals(otp)) {
              System.out.println("Payment method B detected. Simulating a 5-second processing delay...");
              Thread.sleep(5000);
              System.out.println("Processing for payment B completed.");
              latestPayment.setStatus("SUCCESS");
              paymentRepository.save(latestPayment); // version ++
              return new PaymentResponse(latestPayment.getId(), latestPayment.getOrderId(), "SUCCESS",
                  "Payment approved");
            } else {
              latestPayment.setStatus("FAILED");
              paymentRepository.save(latestPayment);
              return new PaymentResponse(latestPayment.getId(), latestPayment.getOrderId(), "FAILED", "Invalid OTP");
            }
          } catch (ObjectOptimisticLockingFailureException e) {
            retries++;
            System.err
                .println("Optimistic locking failure detected for method B, retrying... " + retries + "/" + maxRetries);
            if (retries >= maxRetries) {
              throw new ResponseStatusException(HttpStatus.CONFLICT,
                  "Could not approve payment due to concurrent updates.");
            }

            try {
              Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
              Thread.currentThread().interrupt();
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing was interrupted.", e);
          }
        }
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method: " + paymentMethod);
      }

      if (isValid) {
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);
        return new PaymentResponse(payment.getId(), payment.getOrderId(), "SUCCESS", "Payment approved");
      }
    }

    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found.");
  }

  public Optional<Payment> getPaymentById(String id) {
    return paymentRepository.findById(id);
  }

  public List<Payment> getAllPayments(int page, int size) {
    // System.out.println("Page: " + page + ", Size: " + size);
    return paymentRepository.findAll().subList(page * size,
        Math.min((page + 1) * size, paymentRepository.findAll().size()));
  }
}