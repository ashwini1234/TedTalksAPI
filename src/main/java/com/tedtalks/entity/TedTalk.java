package com.tedtalks.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Table(name = "ted_talks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TedTalk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title" , nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    //private int year;
    @Column(name = "views", nullable = false)
    private BigInteger views;

    @Column(name = "likes", nullable = false)
    private BigInteger likes;

    @Column(name = "date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Column(name = "link", unique = true)
    private String link;

    @Transient
    private BigDecimal influenceScore;
}
