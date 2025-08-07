package com.example.payment.payment_gateway;

import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.Optional;
// import org.springframework.data.jpa.repository.Lock;
// import org.springframework.stereotype.Repository;

// import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}

// @Repository
// public interface PaymentRepository extends JpaRepository<Payment, String> {
// @Lock(LockModeType.PESSIMISTIC_WRITE)
// Optional<Payment> findById(String id);
// }