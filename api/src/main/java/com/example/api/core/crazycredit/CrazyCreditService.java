package com.example.api.core.crazycredit;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface CrazyCreditService {

	/**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/crazy-credit \
     *   -H "Content-Type: application/json" --data \
     *   '{"movieId":123,"crazyCreditId":456,"content":"Some content","spoiler":false}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/crazy-credit",
        consumes = "application/json",
        produces = "application/json")
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
    List<CrazyCredit> getCrazyCredits(@RequestParam(value = "movieId", required = true) int movieId);
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/crazy-credit?movieId=1
     *
     * @param movieId
     */
    @DeleteMapping(value = "/crazy-credit")
    void deleteCrazyCredits(@RequestParam(value = "movieId", required = true)  int movieId);
}
