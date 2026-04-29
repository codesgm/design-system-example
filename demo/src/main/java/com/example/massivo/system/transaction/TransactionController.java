package com.example.massivo.system.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Transaction> list(
            @RequestParam UUID companyId,
            @RequestParam(required = false) UUID financialAccountId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate to,
            @RequestParam(required = false) String nsu,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return service.findFiltered(companyId, financialAccountId, from, to, nsu, tipo, search, pageable);
    }
}
