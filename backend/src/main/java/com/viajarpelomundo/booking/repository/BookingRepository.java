package com.viajarpelomundo.booking.repository;

import com.viajarpelomundo.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // O PostgreSQL filtra as reservas pelo proprietário.
    org.springframework.data.domain.Page<Booking> findByUser_Email(
        String email,
        org.springframework.data.domain.Pageable pageable
    );


    Optional<Booking> findByReferenceAndUser_Email(
        String reference,
        String email
    );
}
