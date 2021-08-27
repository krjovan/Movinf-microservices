package com.example.api.core.crazycredit;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

public interface CrazyCreditService {

    CrazyCredit createCrazyCredit(@RequestBody CrazyCredit body);
	
	/**
     * Sample usage: curl $HOST:$PORT/crazy-credit?movieId=1
     *
     * @param movieId
     * @return
     */
    @GetMapping(
        value    = "/crazy-credit",
        produces = "application/json")
    Flux<CrazyCredit> getCrazyCredits(@RequestParam(value = "movieId", required = true) int movieId);
    
    void deleteCrazyCredits(@RequestParam(value = "movieId", required = true)  int movieId);
}
