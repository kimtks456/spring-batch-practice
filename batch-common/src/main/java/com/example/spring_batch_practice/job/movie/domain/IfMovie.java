package com.example.spring_batch_practice.job.movie.domain;

import com.example.spring_batch_practice.job.movie.client.dto.MovieApiDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "if_movie")
@Data
@NoArgsConstructor
public class IfMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private String externalId;

    private String title;
    private String genre;
    private BigDecimal rating;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "loaded_at")
    private LocalDateTime loadedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static IfMovie from(MovieApiDto dto) {
        IfMovie movie = new IfMovie();
        movie.setExternalId(dto.getExternalId());
        movie.setTitle(dto.getTitle());
        movie.setGenre(dto.getGenre());
        movie.setRating(dto.getRating());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setLoadedAt(LocalDateTime.now());
        return movie;
    }
}
