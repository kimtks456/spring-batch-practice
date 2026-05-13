package com.example.spring_batch_practice.job.movie.reader;

import com.example.spring_batch_practice.job.movie.client.MovieApiClient;
import com.example.spring_batch_practice.job.movie.client.dto.MovieApiDto;
import org.springframework.batch.item.ItemReader;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class FeignMovieItemReader implements ItemReader<MovieApiDto> {

    private final MovieApiClient movieApiClient;
    private final Queue<MovieApiDto> buffer = new ArrayDeque<>();
    private int currentPage = 0;
    private boolean done = false;

    public FeignMovieItemReader(MovieApiClient movieApiClient) {
        this.movieApiClient = movieApiClient;
    }

    @Override
    public MovieApiDto read() {
        if (!buffer.isEmpty()) return buffer.poll();
        if (done) return null;

        List<MovieApiDto> page = movieApiClient.getMovies(currentPage++);
        if (page == null || page.isEmpty()) {
            done = true;
            return null;
        }
        buffer.addAll(page);
        return buffer.poll();
    }
}
