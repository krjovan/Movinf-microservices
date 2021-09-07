package com.example.microservices.core.movie.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import com.example.api.core.movie.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

import java.util.Random;

import com.example.microservices.core.movie.persistence.MovieEntity;
import com.example.microservices.core.movie.persistence.MovieRepository;

import static reactor.core.publisher.Mono.error;

@RestController
public class MovieServiceImpl implements MovieService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieServiceImpl.class);

    private final ServiceUtil serviceUtil;
    
    private final MovieRepository repository;

    private final MovieMapper mapper;

    @Autowired
    public MovieServiceImpl(MovieRepository repository, MovieMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }
    
    @Override
    public Movie createMovie(Movie body) {
    	if (body.getMovieId() < 1) throw new InvalidInputException("Invalid movieId: " + body.getMovieId());

        MovieEntity entity = mapper.apiToEntity(body);
        Mono<Movie> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Movie Id: " + body.getMovieId()))
            .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }

    @Override
    public Mono<Movie> getMovie(int movieId, int delay, int faultPercent) {
        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
        
        if (delay > 0) simulateDelay(delay);

        if (faultPercent > 0) throwErrorIfBadLuck(faultPercent);

        return repository.findByMovieId(movieId)
                .switchIfEmpty(error(new NotFoundException("No movie found for movieId: " + movieId)))
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }
    
    @Override
    public void deleteMovie(int movieId) {
    	if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
        LOG.debug("deleteMovie: tries to delete an entity with movieId: {}", movieId);
        repository.findByMovieId(movieId).log().map(e -> repository.delete(e)).flatMap(e -> e).block();
    }
    
    private void simulateDelay(int delay) {
        LOG.debug("Sleeping for {} seconds...", delay);
        try {Thread.sleep(delay * 1000);} catch (InterruptedException e) {}
        LOG.debug("Moving on...");
    }

    private void throwErrorIfBadLuck(int faultPercent) {
        int randomThreshold = getRandomNumber(1, 100);
        if (faultPercent < randomThreshold) {
            LOG.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
        } else {
            LOG.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
            throw new RuntimeException("Something went wrong...");
        }
    }

    private final Random randomNumberGenerator = new Random();
    private int getRandomNumber(int min, int max) {

        if (max < min) {
            throw new RuntimeException("Max must be greater than min");
        }

        return randomNumberGenerator.nextInt((max - min) + 1) + min;
    }
}