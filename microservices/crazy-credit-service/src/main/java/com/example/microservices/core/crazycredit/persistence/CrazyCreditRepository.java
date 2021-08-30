package com.example.microservices.core.crazycredit.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CrazyCreditRepository extends ReactiveCrudRepository<CrazyCreditEntity, String> {
    Flux<CrazyCreditEntity> findByMovieId(int movieId);
}
