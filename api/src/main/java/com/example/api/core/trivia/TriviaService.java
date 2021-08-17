package com.example.api.core.trivia;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface TriviaService {

	/**
	 * Sample usage:
     *
     * curl -X POST $HOST:$PORT/trivia \
     *   -H "Content-Type: application/json" --data \
     *   '{"movieId":123,"triviaId":456,"publishDate":"2021-08-12","content":"Some content","spoiler":false}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/trivia",
        consumes = "application/json",
        produces = "application/json")
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
    List<Trivia> getTrivia(@RequestParam(value = "movieId", required = true) int movieId);
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/trivia?movieId=1
     *
     * @param movieId
     */
    @DeleteMapping(value = "/trivia")
    void deleteTrivia(@RequestParam(value = "movieId", required = true)  int movieId);
}
