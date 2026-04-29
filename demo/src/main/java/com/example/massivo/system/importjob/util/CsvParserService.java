package com.example.massivo.system.importjob.util;

import com.example.massivo.common.exception.BusinessValidationException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class CsvParserService {

    private static final List<String> EXPECTED_HEADER = List.of(
            "DESCRICAO", "CONTA BANCARIA", "DATA COMPETENCIA", "DATA VENCIMENTO",
            "DATA LANCAMENTO", "VALOR TOTAL", "NSU", "TIPO"
    );
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CSVReader createReader(InputStream input) {
        return new CSVReaderBuilder(new InputStreamReader(input, StandardCharsets.UTF_8)).build();
    }

    public void validateHeader(String[] header) {
        if (header == null || header.length != 8) {
            throw new BusinessValidationException("Header inválido: esperado 8 colunas, recebido " + (header == null ? 0 : header.length));
        }
        List<String> actual = Arrays.stream(header).map(String::trim).map(String::toUpperCase).toList();
        if (!actual.equals(EXPECTED_HEADER)) {
            throw new BusinessValidationException("Header inválido. Esperado: " + EXPECTED_HEADER + ", recebido: " + actual);
        }
    }

    public CsvRow parseRow(String[] line, int rowNumber) {
        if (line.length != 8) {
            return CsvRow.error(rowNumber, String.join(",", line), "FORMAT", "Esperado 8 colunas, recebido " + line.length);
        }

        String descricao = line[0].trim();
        String contaBancaria = line[1].trim();
        String dataCompetenciaStr = line[2].trim();
        String dataVencimentoStr = line[3].trim();
        String dataLancamentoStr = line[4].trim();
        String valorTotalStr = line[5].trim();
        String nsu = line[6].trim();
        String tipo = line[7].trim();

        // Campos obrigatórios
        if (descricao.isEmpty() || contaBancaria.isEmpty() || dataLancamentoStr.isEmpty()
                || valorTotalStr.isEmpty() || nsu.isEmpty() || tipo.isEmpty()) {
            return CsvRow.error(rowNumber, String.join(",", line), "VALIDATION", "Campos obrigatórios faltando");
        }

        // Tipo
        if (!tipo.equalsIgnoreCase("Receita") && !tipo.equalsIgnoreCase("Despesa")) {
            return CsvRow.error(rowNumber, String.join(",", line), "VALIDATION", "Tipo inválido: " + tipo + ". Esperado: Receita ou Despesa");
        }

        // Valor
        BigDecimal valorTotal;
        try {
            valorTotal = new BigDecimal(valorTotalStr.replace(",", "."));
        } catch (NumberFormatException e) {
            return CsvRow.error(rowNumber, String.join(",", line), "VALIDATION", "Valor inválido: " + valorTotalStr);
        }

        // Datas
        LocalDate dataLancamento;
        try {
            dataLancamento = LocalDate.parse(dataLancamentoStr, DATE_FMT);
        } catch (Exception e) {
            return CsvRow.error(rowNumber, String.join(",", line), "VALIDATION", "Data lançamento inválida: " + dataLancamentoStr);
        }

        LocalDate dataCompetencia = parseOptionalDate(dataCompetenciaStr);
        LocalDate dataVencimento = parseOptionalDate(dataVencimentoStr);

        return CsvRow.valid(rowNumber, descricao, contaBancaria, dataCompetencia, dataVencimento, dataLancamento, valorTotal, nsu, tipo);
    }

    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return LocalDate.parse(value, DATE_FMT); }
        catch (Exception e) { return null; }
    }
}
