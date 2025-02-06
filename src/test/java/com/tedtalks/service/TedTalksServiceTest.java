package com.tedtalks.service;

import com.tedtalks.entity.TedTalk;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TedTalksServiceTest {

    @InjectMocks
    private TedTalksService tedTalksService;

    private TedTalk tedTalk1;
    private TedTalk tedTalk2;

    @BeforeEach
    void setUp() {
        tedTalk1 = TedTalk.builder()
                .id(1L)
                .title("Your self-driving robotaxi is almost here")
                .author("Aicha Evans")
                .views(new BigInteger("1300000"))
                .likes(new BigInteger("4031405795013456675"))
                .date(LocalDate.of(2022, 5, 10))
                .link("https://ted.com/talks/the_power_of_ai")
                .build();

        tedTalk2 = TedTalk.builder()
                .id(2L)
                .title("Climate Change Solutions")
                .author("Jane Smith")
                .views(new BigInteger("72000000"))
                .likes(new BigInteger("2100000"))
                .date(LocalDate.of(2006, 2, 1))
                .link("https://ted.com/talks/climate_change_solutions")
                .build();
    }


    @Test
    void testCalculateInfluenceScore() {
        BigDecimal viewsWeight = new BigDecimal("0.4");
        BigDecimal likesWeight = new BigDecimal("0.4");
        BigDecimal engagementWeight = new BigDecimal("0.1");
        BigDecimal growthWeight = new BigDecimal("0.1");

        BigDecimal influenceScore = tedTalksService.calculateInfluenceScore(
                tedTalk2, viewsWeight, likesWeight, engagementWeight, growthWeight
        );

        assertNotNull(influenceScore);
        System.out.println("InfluenceScore= "+ influenceScore);
        assertTrue(influenceScore.compareTo(BigDecimal.ZERO) > 0);
    }


    @Test
    void testCalculateEngagementRate() {
        BigDecimal engagementRate = tedTalksService.calculateEngagementRate(tedTalk1);

        assertNotNull(engagementRate);
        System.out.println("EngagementRate= "+ engagementRate);
        assertTrue(engagementRate.compareTo(BigDecimal.ZERO) > 0);
    }


    @Test
    void testCalculateGrowthRate() {
        BigDecimal growthRate = tedTalksService.calculateGrowthRate(tedTalk1);

        assertNotNull(growthRate);
        System.out.println("Growth Rate= "+ growthRate);
        assertTrue(growthRate.compareTo(BigDecimal.ZERO) > 0);
    }
}
