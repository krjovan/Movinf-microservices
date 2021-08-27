package com.example.api.core.trivia;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

public interface TriviaService {

    Trivia createTrivia(@RequestBody Trivia body);
	
    /**
     * Sample usage: curl $HOST:$PORT/trivia?movieId=1
     *
     * @param movieId
     * @return
     */
    @GetMapping(
        value    = "/trivia",
        produces = "application/json")
    Flux<Trivia> getTrivia(@RequestParam(value = "movieId", required = true) int movieId);
    
    void deleteTrivia(@RequestParam(value = "movieId", required = true)  int movieId);
}
