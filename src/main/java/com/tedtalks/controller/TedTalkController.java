package com.tedtalks.controller;

import com.tedtalks.entity.TedTalk;
import com.tedtalks.exception.BadRequestException;
import com.tedtalks.service.CsvImportService;
import com.tedtalks.service.TedTalksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/tedtalks")
public class TedTalkController {
    private final CsvImportService csvImportService;
    private final TedTalksService tedTalksService;

    public TedTalkController(CsvImportService csvImportService, TedTalksService tedTalksService) {
        this.csvImportService = csvImportService;
        this.tedTalksService = tedTalksService;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> importCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "CSV file is required"));
        }
        try {
            csvImportService.importCsv(file);
            return ResponseEntity.ok(Map.of("message", "CSV file imported successfully"));
        } catch (BadRequestException e) {
            log.error("CSV Import failed: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to process CSV file"));
        }
    }

    @GetMapping
    public Page<TedTalk> getTedTalks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes,desc") String sort) {
        return tedTalksService.getAllTedTalks(page, size, sort);
    }

    @GetMapping("/author/{author}")
    public List<TedTalk> getTedTalksByAuthor(@PathVariable String author) {
        return tedTalksService.getTedTalksByAuthor(author);
    }

    @GetMapping("/year/{year}")
    public List<TedTalk> getTedTalksPerYear(@PathVariable int year) {
        return tedTalksService.getTedTalksTalkPerYear(year);
    }

    @GetMapping("/most-influential/views")
    public TedTalk getMostInfluentialByViews() {
        return tedTalksService.getMostInfluentialByViews();
    }

    @GetMapping("/most-influential/likes")
    public TedTalk getMostInfluentialByLikes() {
        return tedTalksService.getMostInfluentialByLikes();
    }

    @GetMapping("/tedTalksWithInfluenceScore")
    public List<TedTalk> getAllTedTalksWithInfluenceScore(
            @RequestParam(defaultValue = "0.4") BigDecimal viewsWeight,
            @RequestParam(defaultValue = "0.4") BigDecimal likesWeight,
            @RequestParam(defaultValue = "0.1") BigDecimal engagementWeight,
            @RequestParam(defaultValue = "0.1") BigDecimal growthWeight
    ) {
        return tedTalksService.getAllTedTalksWithInfluenceScore(viewsWeight, likesWeight, engagementWeight, growthWeight);
    }

   @GetMapping("/most-influential")
    public ResponseEntity<TedTalk> getMostInfluentialSpeaker(
           @RequestParam(defaultValue = "0.4") BigDecimal viewsWeight,
           @RequestParam(defaultValue = "0.4") BigDecimal likesWeight,
           @RequestParam(defaultValue = "0.1") BigDecimal engagementWeight,
           @RequestParam(defaultValue = "0.1") BigDecimal growthWeight
   ) {
        Optional<TedTalk> talk = tedTalksService.getMostInfluentialSpeaker(viewsWeight, likesWeight, engagementWeight, growthWeight);
        return talk.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/most-influential/{year}")
    public ResponseEntity<TedTalk> getMostInfluentialPerYear(@PathVariable int year,
                                                             @RequestParam(defaultValue = "0.4") BigDecimal viewsWeight,
                                                             @RequestParam(defaultValue = "0.4") BigDecimal likesWeight,
                                                             @RequestParam(defaultValue = "0.1") BigDecimal engagementWeight,
                                                             @RequestParam(defaultValue = "0.1") BigDecimal growthWeight) {
        Optional<TedTalk> talk = tedTalksService.getMostInfluentialTalkPerYear(year, viewsWeight, likesWeight, engagementWeight, growthWeight);
        return talk.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}