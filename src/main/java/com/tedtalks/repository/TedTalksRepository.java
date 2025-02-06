package com.tedtalks.repository;

import com.tedtalks.entity.TedTalk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TedTalksRepository extends JpaRepository<TedTalk, Long> {

    List<TedTalk> findByAuthor(String author);

    List<TedTalk> findTop1ByOrderByViewsDesc();

    List<TedTalk> findTop1ByOrderByLikesDesc();

    List<TedTalk> findByDateBetween(LocalDate startDate, LocalDate endDate);

    Page<TedTalk> findAll(Pageable pageable);
}
