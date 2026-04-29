package com.example.massivo.system.importjob;

import com.example.massivo.common.exception.BusinessValidationException;
import com.example.massivo.system.importjob.dto.ImportResponseDTO;
import com.example.massivo.system.importjob.enums.ImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<ImportResponseDTO> createImport(
            @RequestParam UUID companyId,
            @RequestParam MultipartFile file) {

        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".csv")) {
            throw new BusinessValidationException("Apenas arquivos .csv são aceitos");
        }
        if (file.getSize() > 400 * 1024 * 1024) {
            throw new BusinessValidationException("Arquivo excede o limite de 400MB");
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(importService.createImport(companyId, file));
    }

    @GetMapping
    public Page<ImportResponseDTO> list(
            @RequestParam UUID companyId,
            @RequestParam(required = false) ImportStatus status,
            Pageable pageable) {
        return importService.findByCompanyId(companyId, status, pageable);
    }

    @GetMapping("/{id}")
    public ImportResponseDTO findById(@PathVariable UUID id) {
        return importService.findById(id);
    }

    @GetMapping("/{id}/errors")
    public Page<ImportError> getErrors(@PathVariable UUID id, Pageable pageable) {
        return importService.getErrors(id, pageable);
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetAll(@RequestParam UUID companyId) {
        importService.resetAll(companyId);
        return ResponseEntity.noContent().build();
    }
}
