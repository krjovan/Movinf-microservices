package api.core.crazycredit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface CrazyCreditService {

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
}
