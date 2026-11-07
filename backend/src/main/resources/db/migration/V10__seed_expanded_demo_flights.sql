-- Novas rotas fictícias para demonstração do motor de pesquisa.
-- Horários e preços são inventados e não representam ofertas reais.
-- Os horários são armazenados em UTC, seguindo a convenção actual.

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
        ('VP', 'VP601', 'WAW', 'CDG',  7,  3,  99.00),
        ('VP', 'VP602', 'CDG', 'WAW', 15,  3, 109.00),
        ('VP', 'VP603', 'WAW', 'FCO',  9,  3, 119.00),
        ('VP', 'VP604', 'FCO', 'WAW', 17,  3, 129.00),
        ('NX', 'NX605', 'WAW', 'AMS',  8,  2,  89.00),
        ('NX', 'NX606', 'AMS', 'WAW', 18,  2,  99.00),
        ('NX', 'NX607', 'WAW', 'VNO', 10,  2,  69.00),
        ('NX', 'NX608', 'VNO', 'WAW', 19,  2,  79.00),
        ('NX', 'NX609', 'WAW', 'ARN', 11,  2,  95.00),
        ('NX', 'NX610', 'ARN', 'WAW', 17,  2, 105.00),
        ('VP', 'VP611', 'WAW', 'MAD',  6,  4, 145.00),
        ('VP', 'VP612', 'MAD', 'WAW', 16,  4, 155.00),
        ('SK', 'SK613', 'WAW', 'IST', 12,  3, 169.00),
        ('SK', 'SK614', 'IST', 'WAW', 18,  3, 179.00),
        ('SK', 'SK615', 'WAW', 'AUH', 10,  6, 329.00),
        ('SK', 'SK616', 'AUH', 'WAW', 20,  6, 349.00),
        ('ZA', 'ZA617', 'NBJ', 'JNB',  8,  4, 220.00),
        ('ZA', 'ZA618', 'JNB', 'NBJ', 16,  4, 235.00),
        ('ZA', 'ZA619', 'NBJ', 'CPT',  9,  5, 260.00),
        ('ZA', 'ZA620', 'CPT', 'NBJ', 18,  5, 275.00),
        ('ZA', 'ZA621', 'NBJ', 'LOS',  7,  4, 245.00),
        ('ZA', 'ZA622', 'LOS', 'NBJ', 15,  4, 255.00),
        ('ZA', 'ZA623', 'NBJ', 'DXB', 10,  8, 450.00),
        ('ZA', 'ZA624', 'DXB', 'NBJ', 21,  8, 470.00),
        ('ZA', 'ZA625', 'NBJ', 'LAD',  8,  1,  45.00),
        ('ZA', 'ZA626', 'LAD', 'NBJ', 17,  1,  45.00),
        ('VP', 'VP627', 'LIS', 'MAD', 10,  2,  85.00),
        ('VP', 'VP628', 'MAD', 'LIS', 18,  2,  89.00),
        ('SK', 'SK629', 'DXB', 'SIN',  7,  7, 390.00),
        ('SK', 'SK630', 'SIN', 'DXB', 19,  7, 410.00)
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
    airline.id,
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
JOIN airlines airline ON airline.iata_code = r.airline_code
JOIN airports origin ON origin.iata_code = r.origin_code
JOIN airports destination ON destination.iata_code = r.destination_code
CROSS JOIN generate_series(
    date '2026-11-10',
    date '2027-03-31',
    interval '1 day'
) AS days(day);
