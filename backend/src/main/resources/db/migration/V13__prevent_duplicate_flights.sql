-- Impede a criação de voos duplicados.
-- Preserva os voos, reservas e pagamentos existentes.

ALTER TABLE flights
ADD CONSTRAINT uq_flights_number_departure
UNIQUE (flight_number, departure_time);
