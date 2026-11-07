package com.viajarpelomundo.booking.entity;

import com.viajarpelomundo.flight.entity.Flight;
import com.viajarpelomundo.auth.entity.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Representa uma reserva fictícia.
 * Os lugares são descontados quando a reserva é confirmada.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_flight_id")
    private Flight returnFlight;

    // Proprietário da reserva.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Booking() {
    }

    public Booking(
        String reference,
        Flight flight,
        int passengerCount,
        BigDecimal totalPrice,
        String currency
    ) {
        this.reference = reference;
        this.flight = flight;
        this.passengerCount = passengerCount;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.status = "CONFIRMED";
    }

    public Booking(
        String reference,
        Flight flight,
        Flight returnFlight,
        int passengerCount,
        BigDecimal totalPrice,
        String currency
    ) {
        this(reference, flight, passengerCount, totalPrice, currency);
        this.returnFlight = returnFlight;
    }

    public void assignTo(AppUser user) {
        this.user = user;
    }

    public AppUser getUser() { return user; }
    public Long getId() { return id; }
    public String getReference() { return reference; }
    public Flight getFlight() { return flight; }
    public Flight getReturnFlight() { return returnFlight; }
    public Integer getPassengerCount() { return passengerCount; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
