-- Suporte a reservas de ida e volta.
-- NULL significa reserva de só ida.
-- As reservas existentes permanecem inalteradas.

ALTER TABLE bookings
    ADD COLUMN return_flight_id BIGINT
        REFERENCES flights(id);

ALTER TABLE bookings
    ADD CONSTRAINT chk_booking_different_flights
    CHECK (
        return_flight_id IS NULL
        OR return_flight_id <> flight_id
    );

CREATE INDEX idx_bookings_return_flight
    ON bookings(return_flight_id);
