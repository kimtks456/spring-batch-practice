package com.example.spring_batch_practice.job.movie.client;

import com.example.spring_batch_practice.job.movie.client.dto.MovieApiDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "movieApi", url = "${movie.api.url}")
public interface MovieApiClient {

    @GetMapping("/api/movies")
    List<MovieApiDto> getMovies(@RequestParam("page") int page);
}
