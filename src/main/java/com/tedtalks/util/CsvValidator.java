package com.tedtalks.util;

import com.tedtalks.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvValidator {
    public static List<Map<String, Object>> validateCsv(MultipartFile file, Set<String> existingLinks) throws IOException {
        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> linksInCSVFile = new HashSet<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withAllowMissingColumnNames())) {

            if (!validateHeaders(csvParser)) {
                throw new BadRequestException("CSV Validation Failed", List.of(Map.of("error", "CSV file contains empty or invalid column names.")));
            }
            int rowNum = 2;
            for (CSVRecord record : csvParser) {
                validateRow(record, rowNum, errors, existingLinks, linksInCSVFile);
                rowNum++;
            }
        }
        return errors;
    }

    private static void validateRow(CSVRecord record, int rowNum, List<Map<String, Object>> errors, Set<String> existingLinks, Set<String> newLinks) {
        if (!isValidRecord(record)) {
            errors.add(Map.of("row", rowNum, "error", "Missing required fields."));
            return;
        }

        String link = record.get("link").trim();
        // Check if the link already exists in the database
        if (existingLinks.contains(link)) {
            errors.add(Map.of("row", rowNum, "column", "link", "value", link, "error", "Duplicate TED Talk link found."));
            return;
        }

        // Check if the link is duplicated within the same CSV file
        if (!newLinks.add(link)) {
            errors.add(Map.of(
                    "row", rowNum,
                    "column", "link",
                    "value", link,
                    "error", "Duplicate TED Talk link found in the same CSV file."
            ));
            return;
        }
        validateNumber(record.get("views"), "views", rowNum, errors);
        validateNumber(record.get("likes"), "likes", rowNum, errors);
        validateDate(record.get("date"), rowNum, errors);
    }

    private static void validateNumber(String value, String columnName, int rowNum, List<Map<String, Object>> errors) {
        try {
            BigInteger number = new BigInteger(value.trim());
            if (number.compareTo(BigInteger.ZERO) < 0) {
                errors.add(Map.of(
                        "row", rowNum, "column", columnName, "value", value,
                        "error", "Number must be non-negative."
                ));
            }
        } catch (NumberFormatException e) {
            errors.add(Map.of("row", rowNum, "column", columnName, "value", value, "error", "Invalid number format."));
        }
    }

    private static void validateDate(String dateStr, int rowNum, List<Map<String, Object>> errors) {
        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy dd", Locale.ENGLISH);
        try {
            LocalDate parsedDate = LocalDate.parse(dateStr + " 01", FORMATTER);
            if (parsedDate.isAfter(LocalDate.now())) {
                errors.add(Map.of(
                        "row", rowNum, "column", "date", "value", dateStr,
                        "error", "Date cannot be in the future."
                ));
            }
        } catch (Exception e) {
            errors.add(Map.of(
                    "row", rowNum, "column", "date", "value", dateStr,
                    "error", "Invalid date format. Expected format: 'MMMM yyyy' (Example: 'February 2025')."
            ));
        }
    }

    private static boolean validateHeaders(CSVParser csvParser) {
        List<String> requiredHeaders = List.of("title", "author", "date", "views", "likes", "link");
        Map<String, Integer> headerMap = csvParser.getHeaderMap();
        return requiredHeaders.stream().allMatch(headerMap::containsKey);
    }

    /**
     * Validates whether the CSV record contains all required fields and is not empty.
     */
    private static boolean isValidRecord(CSVRecord record) {
        return record.isSet("title") && !record.get("title").isBlank()
                && record.isSet("author") && !record.get("author").isBlank()
                && record.isSet("views") && !record.get("views").isBlank()
                && record.isSet("likes") && !record.get("likes").isBlank()
                && record.isSet("date") && !record.get("date").isBlank()
                && record.isSet("link") && !record.get("link").isBlank();
    }
}