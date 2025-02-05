package com.tedtalks.repository;

import com.tedtalks.entity.TedTalk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TedTalkRepository extends JpaRepository<TedTalk, Long> {
    //List<TedTalk> findBySpeaker(String speaker);
    //List<TedTalk> findByYear(int year);

    Optional<TedTalk> findByLink(String link);

    List<TedTalk> findByAuthor(String author);

    List<TedTalk> findTop1ByOrderByViewsDesc();

    List<TedTalk> findTop1ByOrderByLikesDesc();
}
