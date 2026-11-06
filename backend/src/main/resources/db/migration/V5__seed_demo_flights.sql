-- Dados fictícios para testar o motor de pesquisa.
-- Nenhum preço ou horário corresponde a uma oferta real.

WITH demo_routes (
    airline_code,
    flight_number,
    origin_code,
    destination_code,
    departure_hour,
    duration_hours,
    price_eur
) AS (
    VALUES
        ('VP', 'VP101', 'WAW', 'NBJ', 9, 10, 490.00),
        ('ZA', 'ZA102', 'NBJ', 'WAW', 11, 10, 510.00),
        ('VP', 'VP201', 'WAW', 'LIS', 7, 4, 110.00),
        ('VP', 'VP202', 'LIS', 'WAW', 16, 4, 120.00),
        ('SK', 'SK301', 'WAW', 'DXB', 13, 6, 290.00),
        ('SK', 'SK302', 'DXB', 'WAW', 8, 6, 310.00),
        ('NX', 'NX401', 'WAW', 'LHR', 10, 3, 95.00),
        ('NX', 'NX402', 'LHR', 'WAW', 18, 3, 105.00),
        ('ZA', 'ZA501', 'NBJ', 'LIS', 14, 8, 390.00),
        ('ZA', 'ZA502', 'LIS', 'NBJ', 22, 8, 410.00)
)
INSERT INTO flights (
    flight_number,
    airline_id,
    origin_airport_id,
    destination_airport_id,
    departure_time,
    arrival_time,
    base_price,
    currency,
    total_seats,
    available_seats,
    status
)
SELECT
    r.flight_number,
    a.id,
    origin.id,
    destination.id,
    (
        days.day + make_interval(hours => r.departure_hour)
    ) AT TIME ZONE 'UTC',
    (
        days.day + make_interval(
            hours => r.departure_hour + r.duration_hours
        )
    ) AT TIME ZONE 'UTC',
    r.price_eur,
    'EUR',
    180,
    180,
    'SCHEDULED'
FROM demo_routes r
JOIN airlines a
    ON a.iata_code = r.airline_code
JOIN airports origin
    ON origin.iata_code = r.origin_code
JOIN airports destination
    ON destination.iata_code = r.destination_code
CROSS JOIN generate_series(
    date '2026-11-10',
    date '2027-03-31',
    interval '1 day'
) AS days(day);
