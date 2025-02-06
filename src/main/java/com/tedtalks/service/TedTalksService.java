package com.tedtalks.service;

import com.tedtalks.entity.TedTalk;
import com.tedtalks.repository.TedTalksRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TedTalksService {
    private final TedTalksRepository tedTalksRepository;

    public TedTalksService(TedTalksRepository tedTalksRepository) {
        this.tedTalksRepository = tedTalksRepository;
    }

    public Page<TedTalk> getAllTedTalks(int page, int size, String sort) {
        Sort sortCriteria = parseSortCriteria(sort);
        Pageable pageable = PageRequest.of(page, size, sortCriteria);
        return tedTalksRepository.findAll(pageable);
    }

    /**
     * Parses sorting criteria -- Example: Sort.Direction.DESC, "views"
     *
     * @param sort
     * @return
     */
    private Sort parseSortCriteria(String sort) {
        String[] sortParams = sort.split(",");
        if (sortParams.length == 2) {
            return Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        } else {
            return Sort.by(Sort.Direction.ASC, sortParams[0]); // Default to ascending if no order provided
        }
    }

    public List<TedTalk> getTedTalksByAuthor(String author) {
        return tedTalksRepository.findByAuthor(author);
    }

    public List<TedTalk> getTedTalksTalkPerYear(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return tedTalksRepository.findByDateBetween(startDate, endDate);
    }

    public TedTalk getMostInfluentialByViews() {
        return tedTalksRepository.findTop1ByOrderByViewsDesc().get(0);
    }

    public TedTalk getMostInfluentialByLikes() {
        return tedTalksRepository.findTop1ByOrderByLikesDesc().get(0);
    }


    public Optional<TedTalk> getMostInfluentialSpeaker(BigDecimal viewsWeight, BigDecimal likesWeight,
                                                       BigDecimal engagementWeight, BigDecimal growthWeight) {
        return tedTalksRepository.findAll().stream()
                .max(Comparator.comparing(talk -> calculateInfluenceScore(talk, viewsWeight, likesWeight, engagementWeight, growthWeight)));
    }

    public Optional<TedTalk> getMostInfluentialTalkPerYear(int year, BigDecimal viewsWeight, BigDecimal likesWeight, BigDecimal engagementWeight, BigDecimal growthWeight) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        List<TedTalk> talksForYear = tedTalksRepository.findByDateBetween(startDate, endDate);
        return talksForYear.stream()
                .max(Comparator.comparing(talk -> calculateInfluenceScore(talk, viewsWeight, likesWeight, engagementWeight, growthWeight)));
    }

    public List<TedTalk> getAllTedTalksWithInfluenceScore(BigDecimal viewsWeight, BigDecimal likesWeight, BigDecimal engagementWeight, BigDecimal growthWeight) {
        return tedTalksRepository.findAll().stream()
                .map(talk -> {
                    talk.setInfluenceScore(calculateInfluenceScore(talk, viewsWeight, likesWeight, engagementWeight, growthWeight));
                    return talk;
                })
                .sorted(Comparator.comparing(TedTalk::getInfluenceScore).reversed())
                .toList();
    }

    /**
     * Calculates the Influence Score based on configurable weightages.
     *
     * InfluenceScore=(viewsWeight×TotalViews)+(likesWeight×TotalLikes)+(engagementWeight×EngagementRate)+(growthWeight×GrowthRate)
     *
     */
    public BigDecimal calculateInfluenceScore(TedTalk talk,
                                               BigDecimal viewsWeight, BigDecimal likesWeight,
                                               BigDecimal engagementWeight, BigDecimal growthWeight) {

        BigDecimal engagementRate = calculateEngagementRate(talk);
        BigDecimal growthRate = calculateGrowthRate(talk);

        return new BigDecimal(talk.getViews()).multiply(viewsWeight)
                .add(new BigDecimal(talk.getLikes()).multiply(likesWeight))
                .add(engagementRate.multiply(engagementWeight))
                .add(growthRate.multiply(growthWeight))
                .setScale(5, RoundingMode.HALF_UP); // Precision set to 5 decimal places
    }

    /**
     *    Engagement Rate Formula = (Likes / Views) * 100
     *
     *    Engagement Rate = Percentage of viewers who liked the talk
     */
    public BigDecimal calculateEngagementRate(TedTalk talk) {
        if (talk.getViews().equals(BigInteger.ZERO)) return BigDecimal.ZERO;

        return new BigDecimal(talk.getLikes())
                .divide(new BigDecimal(talk.getViews()), 5, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Growth Rate = (Likes + Views) / DaysSincePublished
     *
     * Measures how fast a talk gains popularity
     *
     * @param talk
     * @return
     */
    public BigDecimal calculateGrowthRate(TedTalk talk) {
        if (talk.getViews().equals(BigInteger.ZERO) || talk.getLikes().equals(BigInteger.ZERO))
            return BigDecimal.ZERO;
        long daysSincePublished = Math.max(1, talk.getDate().until(LocalDate.now()).toTotalMonths() * 30L);
        BigDecimal daysBigDecimal = BigDecimal.valueOf(daysSincePublished);
        BigDecimal viewsPlusLikes= new BigDecimal(talk.getViews()).add(new BigDecimal(talk.getLikes()));
        return viewsPlusLikes.divide(daysBigDecimal, 5, RoundingMode.HALF_UP);
    }
}