package com.tedtalks.controller;

import com.tedtalks.entity.TedTalk;
import com.tedtalks.service.CsvImportService;
import com.tedtalks.service.TedTalksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TedTalkControllerTest {

    private MockMvc mockMvc;
    private TedTalksService tedTalksService;
    private CsvImportService csvImportService;

    @BeforeEach
    void setUp() {
        tedTalksService = mock(TedTalksService.class);
        csvImportService = mock(CsvImportService.class);
        TedTalkController tedTalkController = new TedTalkController(csvImportService, tedTalksService);
        mockMvc = MockMvcBuilders.standaloneSetup(tedTalkController).build();
    }

    // Test : Import CSV File :Happy Case
    @Test
    void testImportCsv_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "tedtalks.csv",
                "text/csv", "title,author,views,likes,date,link\nAI Future,John Doe,1000000,50000,January 2022,https://ted.com/ai_future".getBytes());

        mockMvc.perform(multipart("/tedtalks/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("CSV file imported successfully"));
    }

    // Test : Get TED Talks by Author
    @Test
    void testGetTedTalksByAuthor() throws Exception {
        when(tedTalksService.getTedTalksByAuthor("John Doe"))
                .thenReturn(List.of(new TedTalk(1L, "AI Revolution", "John Doe",
                        BigInteger.valueOf(5000000), BigInteger.valueOf(200000),
                        LocalDate.of(2021, 5, 20), "https://ted.com/ai_revolution", null)));

        mockMvc.perform(get("/tedtalks/author/John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].author").value("John Doe"));
    }

    // Test : Get TED Talks by Year
    @Test
    void testGetTedTalksByYear() throws Exception {
        when(tedTalksService.getTedTalksTalkPerYear(2022))
                .thenReturn(List.of(new TedTalk(1L, "AI in 2022", "Jane Doe",
                        BigInteger.valueOf(3000000), BigInteger.valueOf(100000),
                        LocalDate.of(2022, 6, 15), "https://ted.com/ai_2022", null)));

        mockMvc.perform(get("/tedtalks/year/2022"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("AI in 2022"));
    }



    // Test : Get Most Influential Speaker
    @Test
    void testGetMostInfluentialSpeaker() throws Exception {
        when(tedTalksService.getMostInfluentialSpeaker(any(), any(), any(), any()))
                .thenReturn(Optional.of(new TedTalk(1L, "AI Leadership", "Jane Doe",
                        BigInteger.valueOf(2000000), BigInteger.valueOf(50000),
                        LocalDate.of(2022, 8, 10), "https://ted.com/ai_leadership", null)));

        mockMvc.perform(get("/tedtalks/most-influential")
                        .param("viewsWeight", "0.4")
                        .param("likesWeight", "0.4")
                        .param("engagementWeight", "0.1")
                        .param("growthWeight", "0.1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Jane Doe"));
    }

    // Test: Get Most Influential TED Talk for a Year
    @Test
    void testGetMostInfluentialPerYear() throws Exception {
        when(tedTalksService.getMostInfluentialTalkPerYear(eq(2022), any(), any(), any(), any()))
                .thenReturn(Optional.of(new TedTalk(1L, "AI Breakthrough 2022", "Mark Smith",
                        BigInteger.valueOf(3000000), BigInteger.valueOf(200000),
                        LocalDate.of(2022, 7, 1), "https://ted.com/ai_2022_breakthrough", null)));

        mockMvc.perform(get("/tedtalks/most-influential/2022")
                        .param("viewsWeight", "0.4")
                        .param("likesWeight", "0.4")
                        .param("engagementWeight", "0.1")
                        .param("growthWeight", "0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("AI Breakthrough 2022"));
    }
}