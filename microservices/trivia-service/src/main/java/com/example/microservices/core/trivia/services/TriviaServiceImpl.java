package com.example.microservices.core.trivia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.trivia.persistence.TriviaEntity;
import com.example.microservices.core.trivia.persistence.TriviaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.api.core.trivia.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

@RestController
public class TriviaServiceImpl implements TriviaService {

    private static final Logger LOG = LoggerFactory.getLogger(TriviaServiceImpl.class);
    
    private final TriviaRepository repository;

    private final TriviaMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public TriviaServiceImpl(TriviaRepository repository, TriviaMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Trivia createTrivia(Trivia body) {
    	if (body.getMovieId() < 1) throw new InvalidInputException("Invalid movieId: " + body.getMovieId());

        TriviaEntity entity = mapper.apiToEntity(body);
        Mono<Trivia> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Movie Id: " + body.getMovieId() + ", Trivia Id:" + body.getTriviaId()))
            .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }
    
    @Override
    public Flux<Trivia> getTrivia(int movieId) {
    	if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
    	
    	return repository.findByMovieId(movieId)
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }
    
    @Override
    public void deleteTrivia(int movieId) {
    	if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
        LOG.debug("deleteTrivia: tries to delete trivia for the movie with movieId: {}", movieId);
        repository.deleteAll(repository.findByMovieId(movieId)).block();
    }
}
