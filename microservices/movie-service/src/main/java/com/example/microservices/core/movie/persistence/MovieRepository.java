package com.example.microservices.core.movie.persistence;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface MovieRepository extends PagingAndSortingRepository<MovieEntity, String> {
    Optional<MovieEntity> findByMovieId(int movieId);
}
