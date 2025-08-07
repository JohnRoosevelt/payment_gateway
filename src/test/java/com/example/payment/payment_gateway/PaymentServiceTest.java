package com.example.payment.payment_gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Commit;

@SpringBootTest
@Commit
class PaymentServiceTest {

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private PaymentService paymentService;

  private String TEST_PAYMENT_ID;
  private static final int NUM_THREADS = 3;

  /**
   * Sets up the initial test data before each test.
   * It cleans up the repository and creates a new PENDING payment with method
   * 'B'.
   */
  @BeforeEach
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void setup() {
    paymentRepository.deleteAll();

    Payment payment = new Payment();

    payment.setOrderId("test-order-123");
    payment.setAmount(100.0);
    payment.setCurrency("USD");
    payment.setStatus("PENDING");
    payment.setPaymentMethod("B");

    Payment savedPayment = paymentRepository.save(payment);
    TEST_PAYMENT_ID = savedPayment.getId();
  }

  /**
   * This test simulates a race condition where multiple threads try to approve
   * the same payment
   * concurrently. It verifies that the optimistic locking mechanism correctly
   * handles
   * the conflict and allows only one request to succeed.
   *
   * @throws InterruptedException if the test thread is interrupted while waiting.
   */
  @Test
  void testConcurrentApprovalsWithOptimisticLocking() throws InterruptedException {
    // Create a fixed-size thread pool to simulate concurrent requests.
    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    // CountDownLatch is used to make all threads start at the same time.
    CountDownLatch startLatch = new CountDownLatch(1);

    // AtomicIntegers to safely count successful and failed attempts.
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // Submit multiple tasks (concurrent requests) to the executor.
    for (int i = 0; i < NUM_THREADS; i++) {
      executor.submit(() -> {
        try {
          // Wait for the main thread to release the latch, ensuring all threads
          // are ready to start simultaneously.
          startLatch.await();

          // Call the service method that contains the optimistic locking logic.
          paymentService.approvePayment(TEST_PAYMENT_ID, null, "999999");
          successCount.incrementAndGet();
        } catch (ObjectOptimisticLockingFailureException e) {
          // This is the expected behavior for concurrent failures.
          System.err.println("Thread failed with optimistic locking error.");
          failureCount.incrementAndGet();
        } catch (Exception e) {
          // Catch any other unexpected exceptions.
          System.err.println("Thread failed with generic error: " + e.getMessage());
          failureCount.incrementAndGet();
        }
      });
    }

    // Release the latch to start all threads at once.
    startLatch.countDown();

    // Wait for all submitted tasks to complete or for a timeout to occur.
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    // --- Assertions to verify the final state and counts ---

    // 1. Verify the final state of the payment in the database.
    Optional<Payment> finalPayment = paymentRepository.findById(TEST_PAYMENT_ID);
    assertTrue(finalPayment.isPresent());
    assertEquals("SUCCESS", finalPayment.get().getStatus(), "Final payment status should be SUCCESS");

    // After the initial save (version 0) and one successful update, the version
    // should be 1.
    assertEquals(1L, finalPayment.get().getVersion(), "Version should be 1 after one successful update");

    // 2. Verify the outcome counts of the concurrent requests.
    // Due to the optimistic locking and retry logic, only one thread should
    // succeed.
    // The other threads will encounter an optimistic lock exception and fail.
    assertEquals(1, successCount.get(), "Only one request should succeed");
    assertEquals(2, failureCount.get(), "Two requests should fail due to concurrency issues");
  }
}