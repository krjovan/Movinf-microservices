package com.example.microservices.core.trivia.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TriviaRepository extends ReactiveCrudRepository<TriviaEntity, String> {
    Flux<TriviaEntity> findByMovieId(int movieId);
}
