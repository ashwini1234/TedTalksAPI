package com.tedtalks.service;


import com.tedtalks.exception.BadRequestException;
import com.tedtalks.repository.TedTalksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CsvImportServiceTest {

    private CsvImportService csvImportService;
    @Mock
    private TedTalksRepository tedTalksRepository;
    @Mock
    private TedTalksService tedTalksService;

    @BeforeEach
    void setUp() {
        tedTalksRepository = mock(TedTalksRepository.class);
        csvImportService = new CsvImportService(tedTalksRepository, tedTalksService);
    }

    @Test
    void testImportCsv_Successful() throws IOException {
        String csvContent = "title,author,views,likes,date,link\n" +
                "AI in Healthcare,John Doe,1000000,50000,January 2022,https://ted.com/talks/ai_healthcare";
        MockMultipartFile file = new MockMultipartFile("file", "tedtalks.csv",
                "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        csvImportService.importCsv(file);

        verify(tedTalksRepository, times(1)).saveAll(anyList());
    }

    // Empty File
    @Test
    void testImportCsv_EmptyFile_ShouldThrowBadRequestException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv",
                "text/csv", new byte[0]);

        Exception exception = assertThrows(BadRequestException.class, () -> {
            csvImportService.importCsv(emptyFile);
        });

        System.out.println(exception.getMessage());
    }

    //  Invalid Data Type in Views
    @Test
    void testImportCsv_InvalidDataType_ShouldThrowBadRequestException() throws BadRequestException {
        String invalidDataCsv = "title,author,views,likes,date,link\n" +
                "Future of AI,John Doe,INVALID_NUMBER,50000,January 2022,https://ted.com/ai_future";

        MockMultipartFile file = new MockMultipartFile("file", "tedtalks.csv",
                "text/csv", invalidDataCsv.getBytes(StandardCharsets.UTF_8));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            csvImportService.importCsv(file);
        });
        System.out.println(exception.getMessage());
    }

    // Invalid Date Format
    @Test
    void testImportCsv_InvalidDateFormat_ShouldThrowBadRequestException() throws BadRequestException {
        String invalidDateCsv = "title,author,views,likes,date,link\n" +
                "Future of AI,John Doe,1000000,50000,InvalidDate,https://ted.com/ai_future";

        MockMultipartFile file = new MockMultipartFile("file", "tedtalks.csv",
                "text/csv", invalidDateCsv.getBytes(StandardCharsets.UTF_8));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            csvImportService.importCsv(file);
        });
        System.out.println(exception.getMessage());
    }

    //  Duplicate TED Talk (Same Link)
    @Test
    void testImportCsv_DuplicateEntry_ShouldThrowBadRequestException() throws BadRequestException {
        String duplicateCsv = "title,author,views,likes,date,link\n" +
                "AI Revolution,Jane Doe,2000000,100000,February 2022,https://ted.com/ai_revolution\n" +
                "AI Revolution,Jane Doe,2000000,100000,February 2022,https://ted.com/ai_revolution";

        MockMultipartFile file = new MockMultipartFile("file", "tedtalks.csv",
                "text/csv", duplicateCsv.getBytes(StandardCharsets.UTF_8));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            csvImportService.importCsv(file);
        });
        System.out.println(exception.getMessage());
    }
}