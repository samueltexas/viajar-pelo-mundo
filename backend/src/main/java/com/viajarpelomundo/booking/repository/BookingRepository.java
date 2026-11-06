package com.viajarpelomundo.booking.repository;

import com.viajarpelomundo.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReferenceAndUser_Email(
        String reference,
        String email
    );
}
