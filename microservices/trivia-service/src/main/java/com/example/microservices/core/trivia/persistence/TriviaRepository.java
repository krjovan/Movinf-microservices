package com.example.microservices.core.trivia.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TriviaRepository extends CrudRepository<TriviaEntity, String> {
    List<TriviaEntity> findByMovieId(int movieId);
}
