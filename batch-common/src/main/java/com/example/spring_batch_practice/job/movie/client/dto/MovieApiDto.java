package com.example.spring_batch_practice.job.movie.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieApiDto {
    private String externalId;
    private String title;
    private String genre;
    private BigDecimal rating;
    private LocalDate releaseDate;
}
