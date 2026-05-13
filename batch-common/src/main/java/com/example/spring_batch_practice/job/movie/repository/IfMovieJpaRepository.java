package com.example.spring_batch_practice.job.movie.repository;

import com.example.spring_batch_practice.job.movie.domain.IfMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IfMovieJpaRepository extends JpaRepository<IfMovie, Long> {

    Optional<IfMovie> findByExternalId(String externalId);
}
