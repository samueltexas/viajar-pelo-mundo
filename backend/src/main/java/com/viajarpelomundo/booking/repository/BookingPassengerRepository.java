package com.viajarpelomundo.booking.repository;

import com.viajarpelomundo.booking.entity.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingPassengerRepository
    extends JpaRepository<BookingPassenger, Long> {

    List<BookingPassenger> findByBooking_IdOrderByIdAsc(
        Long bookingId
    );
}
