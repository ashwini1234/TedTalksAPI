package com.tedtalks.service;

import com.tedtalks.entity.TedTalk;
import com.tedtalks.exception.BadRequestException;
import com.tedtalks.repository.TedTalksRepository;
import com.tedtalks.util.CsvValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsvImportService {
    private final TedTalksRepository tedTalksRepository;
    private final TedTalksService tedTalksService;

    public CsvImportService(TedTalksRepository tedTalksRepository, TedTalksService tedTalksService) {
        this.tedTalksRepository = tedTalksRepository;
        this.tedTalksService = tedTalksService;
    }

    public void importCsv(MultipartFile file) throws IOException {
        // Fetch all existing TED Talk links to avoid duplicate DB calls
        Set<String> existingLinks = tedTalksRepository.findAll().stream()
                .map(TedTalk::getLink)
                .collect(Collectors.toSet());

        // Step 1: Validate CSV file before proceeding
        List<Map<String, Object>> errors = CsvValidator.validateCsv(file, existingLinks);
        if (!errors.isEmpty()) {
            log.error("CSV Validation Failed");
            throw new BadRequestException("CSV Validation Failed", errors);
        }

        // Step 2: Import Data (Since validation passed)
        List<TedTalk> tedTalks = processCsv(file);
        tedTalksRepository.saveAll(tedTalks);
        log.info("{} TED Talks imported successfully!", tedTalks.size());
    }

    public List<TedTalk> processCsv(MultipartFile file) throws IOException {
        List<TedTalk> tedTalks = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withAllowMissingColumnNames())) {

            for (CSVRecord record : csvParser) {
                TedTalk talk = mapRecordToTedTalk(record);
                if (talk != null) {
                    tedTalks.add(talk);
                }
            }
        }
        log.info("{} TED Talks successfully processed for import.", tedTalks.size());
        return tedTalks;
    }

    private TedTalk mapRecordToTedTalk(CSVRecord record) {
        try {
            BigInteger views = new BigInteger(record.get("views").trim());
            BigInteger likes = new BigInteger(record.get("likes").trim());
            LocalDate date= LocalDate.parse(record.get("date") + " 01", DateTimeFormatter.ofPattern("MMMM yyyy dd", Locale.ENGLISH));

            return TedTalk.builder()
                    .title(record.get("title").trim())
                    .author(record.get("author").trim())
                    .views(views)
                    .likes(likes)
                    .date(date)
                    .link(record.get("link").trim())
                    .build();
        } catch (Exception e) {
            log.error("Skipping invalid row: {} | Error: {}", record.toString(), e.getMessage());
            return null;
        }
    }
}