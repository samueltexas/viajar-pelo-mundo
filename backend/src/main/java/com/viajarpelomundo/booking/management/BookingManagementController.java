package com.viajarpelomundo.booking.management;

import com.viajarpelomundo.booking.dto.BookingResponse;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de gestão das reservas do utilizador autenticado.
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingManagementController {

    private final BookingManagementService service;

    public BookingManagementController(
        BookingManagementService service
    ) {
        this.service = service;
    }

    @GetMapping("/mine")
    public List<BookingResponse> mine() {
        return service.mine();
    }

    @PostMapping("/{reference}/cancel")
    public BookingResponse cancel(
        @PathVariable String reference
    ) {
        return service.cancel(reference);
    }

    @GetMapping("/{reference}/payment")
    public Map<String, Object> payment(
        @PathVariable String reference
    ) {
        return service.payment(reference);
    }

    @PostMapping("/{reference}/pay")
    public Map<String, Object> pay(
        @PathVariable String reference
    ) {
        return service.pay(reference);
    }
}
