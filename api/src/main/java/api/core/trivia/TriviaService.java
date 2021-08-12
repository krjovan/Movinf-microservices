package api.core.trivia;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface TriviaService {

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
}
