package com.example.api.core.review;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

public interface ReviewService {

    Review createReview(@RequestBody Review body);
	
    /**
     * Sample usage: curl $HOST:$PORT/review?movieId=1
     *
     * @param movieId
     * @return
     */
    @GetMapping(
        value    = "/review",
        produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "movieId", required = true) int movieId);
    
    void deleteReviews(@RequestParam(value = "movieId", required = true)  int movieId);
}