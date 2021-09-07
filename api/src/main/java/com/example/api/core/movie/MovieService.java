package com.example.api.core.movie;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface MovieService {

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
    Mono<Movie> getMovie(
            @PathVariable int movieId,
            @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
            @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent
       );

    void deleteMovie(@PathVariable int movieId);
}
