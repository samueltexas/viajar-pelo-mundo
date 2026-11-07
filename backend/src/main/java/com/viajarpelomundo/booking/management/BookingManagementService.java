package com.viajarpelomundo.booking.management;

import com.viajarpelomundo.booking.dto.BookingResponse;
import com.viajarpelomundo.booking.repository.BookingRepository;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Gere o histórico, cancelamentos e pagamentos de demonstração.
 *
 * Todas as operações verificam o proprietário da reserva.
 * Os cancelamentos usam transacções para evitar a perda de lugares.
 */
@Service
public class BookingManagementService {

    private final BookingRepository bookings;
    private final JdbcTemplate jdbc;

    public BookingManagementService(
        BookingRepository bookings,
        JdbcTemplate jdbc
    ) {
        this.bookings = bookings;
        this.jdbc = jdbc;
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<BookingResponse> mine(
        int page,
        int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page must be >= 0 and size must be between 1 and 100"
            );
        }

        var pageable = org.springframework.data.domain.PageRequest.of(
            page,
            size,
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC,
                "createdAt"
            )
        );

        return bookings.findByUser_Email(
            currentEmail(),
            pageable
        ).map(BookingResponse::from);
    }

    @Transactional
    public BookingResponse cancel(String reference) {
        String email = currentEmail();

        // O UPDATE condicional impede que a mesma reserva
        // seja cancelada duas vezes.
        List<Map<String, Object>> changed = jdbc.queryForList("""
            UPDATE bookings
            SET status = 'CANCELLED'
            WHERE reference = ?
              AND status = 'CONFIRMED'
              AND user_id = (
                  SELECT id FROM app_users WHERE email = ?
              )
            RETURNING id, flight_id, passenger_count
            """, reference, email);

        if (changed.isEmpty()) {
            var booking = bookings.findByReferenceAndUser_Email(
                reference, email
            ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found"
            ));

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Booking cannot be cancelled in status "
                    + booking.getStatus()
            );
        }

        Map<String, Object> row = changed.getFirst();

        Long flightId = ((Number) row.get("flight_id")).longValue();
        Long bookingId = ((Number) row.get("id")).longValue();
        int passengers = ((Number) row.get("passenger_count")).intValue();

        // Devolve exactamente os lugares desta reserva.
        int restored = jdbc.update("""
            UPDATE flights
            SET available_seats = available_seats + ?
            WHERE id = ?
              AND available_seats + ? <= total_seats
            """, passengers, flightId, passengers);

        if (restored != 1) {
            throw new IllegalStateException(
                "Unable to restore flight seats"
            );
        }

        // Se já houve pagamento fictício, regista reembolso fictício.
        jdbc.update("""
            UPDATE demo_payments
            SET status = CASE
                WHEN status = 'PAID' THEN 'REFUNDED'
                ELSE 'CANCELLED'
            END,
            updated_at = CURRENT_TIMESTAMP
            WHERE booking_id = ?
            """, bookingId);

        // O estado actualizado é lido directamente da base de dados.
        return findBookingResponse(reference, email);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> payment(String reference) {
        var booking = ownedBooking(reference);

        List<Map<String, Object>> payments = jdbc.queryForList("""
            SELECT status, amount, currency, created_at, updated_at
            FROM demo_payments
            WHERE booking_id = ?
            """, booking.getId());

        if (payments.isEmpty()) {
            return Map.of(
                "bookingReference", reference,
                "status", booking.getStatus().equals("CANCELLED")
                    ? "CANCELLED" : "PENDING",
                "amount", booking.getTotalPrice(),
                "currency", booking.getCurrency(),
                "simulation", true
            );
        }

        var payment = payments.getFirst();

        return Map.of(
            "bookingReference", reference,
            "status", payment.get("status"),
            "amount", payment.get("amount"),
            "currency", payment.get("currency"),
            "simulation", true
        );
    }

    @Transactional
    public Map<String, Object> pay(String reference) {
        var booking = ownedBooking(reference);

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Cancelled bookings cannot be paid"
            );
        }

        // Este pagamento é uma simulação.
        // Nenhum valor é cobrado ao utilizador.
        int inserted = jdbc.update("""
            INSERT INTO demo_payments (
                booking_id, status, amount, currency
            )
            VALUES (?, 'PAID', ?, ?)
            ON CONFLICT (booking_id) DO NOTHING
            """,
            booking.getId(),
            booking.getTotalPrice(),
            booking.getCurrency()
        );

        if (inserted == 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Payment has already been processed"
            );
        }

        return Map.of(
            "bookingReference", reference,
            "status", "PAID",
            "amount", booking.getTotalPrice(),
            "currency", booking.getCurrency(),
            "simulation", true,
            "message", "Demo payment completed"
        );
    }

    private com.viajarpelomundo.booking.entity.Booking ownedBooking(
        String reference
    ) {
        return bookings.findByReferenceAndUser_Email(
            reference,
            currentEmail()
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Booking not found"
        ));
    }

    private BookingResponse findBookingResponse(
        String reference,
        String email
    ) {
        // Evita devolver uma entidade JPA com estado antigo
        // depois de um UPDATE SQL directo.
        jdbc.execute("SELECT 1");

        var row = jdbc.queryForMap("""
            SELECT b.reference, b.flight_id, f.flight_number,
                   b.passenger_count, b.total_price,
                   b.currency, b.status
            FROM bookings b
            JOIN flights f ON f.id = b.flight_id
            JOIN app_users u ON u.id = b.user_id
            WHERE b.reference = ? AND u.email = ?
            """, reference, email);

        return new BookingResponse(
            (String) row.get("reference"),
            ((Number) row.get("flight_id")).longValue(),
            (String) row.get("flight_number"),
            ((Number) row.get("passenger_count")).intValue(),
            (java.math.BigDecimal) row.get("total_price"),
            (String) row.get("currency"),
            (String) row.get("status")
        );
    }
}
