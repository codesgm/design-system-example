package com.example.massivo.system.importjob.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CsvRow(
        int rowNumber, boolean valid,
        String descricao, String contaBancaria,
        LocalDate dataCompetencia, LocalDate dataVencimento, LocalDate dataLancamento,
        BigDecimal valorTotal, String nsu, String tipo,
        String rawContent, String errorType, String errorMessage
) {
    public static CsvRow valid(int row, String descricao, String contaBancaria,
                               LocalDate dataCompetencia, LocalDate dataVencimento, LocalDate dataLancamento,
                               BigDecimal valorTotal, String nsu, String tipo) {
        return new CsvRow(row, true, descricao, contaBancaria, dataCompetencia, dataVencimento, dataLancamento, valorTotal, nsu, tipo, null, null, null);
    }

    public static CsvRow error(int row, String rawContent, String errorType, String errorMessage) {
        return new CsvRow(row, false, null, null, null, null, null, null, null, null, rawContent, errorType, errorMessage);
    }
}
