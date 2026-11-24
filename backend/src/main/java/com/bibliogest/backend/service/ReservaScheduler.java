package com.bibliogest.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaScheduler {

    private final ReservaService reservaService;

    public ReservaScheduler(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void revisarVencimientos() {
        reservaService.vencerReservas();
    }
}