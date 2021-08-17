package com.example.api.core.movie;

import org.springframework.web.bind.annotation.*;

public interface MovieService {

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/movie \
     *   -H "Content-Type: application/json" --data \
     *   '{"movieId":123,"title":"Title 123","releaseDate":"2021-08-12","country":"Some country","budget":0,"gross":0,"runtime":0}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/movie",
        consumes = "application/json",
        produces = "application/json")
    Movie createMovie(@RequestBody Movie body);
	
    /**
     * Sample usage: curl $HOST:$PORT/movie/1
     *
     * @param movieId
     * @return the movie, if found, else null
     */
    @GetMapping(
        value    = "/movie/{movieId}",
        produces = "application/json")
     Movie getMovie(@PathVariable int movieId);
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/movie/1
     *
     * @param movieId
     */
    @DeleteMapping(value = "/movie/{movieId}")
    void deleteMovie(@PathVariable int movieId);
}
