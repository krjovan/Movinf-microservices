package com.example.microservices.core.crazycredit.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditEntity;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.api.core.crazycredit.*;
import com.example.util.exceptions.*;
import com.example.util.http.*;

@RestController
public class CrazyCreditServiceImpl implements CrazyCreditService {

    private static final Logger LOG = LoggerFactory.getLogger(CrazyCreditServiceImpl.class);

    private final ServiceUtil serviceUtil;
    
    private final CrazyCreditRepository repository;

    private final CrazyCreditMapper mapper;

    @Autowired
    public CrazyCreditServiceImpl(CrazyCreditRepository repository, CrazyCreditMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public CrazyCredit createCrazyCredit(CrazyCredit body) {
    	if (body.getMovieId() < 1) throw new InvalidInputException("Invalid movieId: " + body.getMovieId());

        CrazyCreditEntity entity = mapper.apiToEntity(body);
        Mono<CrazyCredit> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Movie Id: " + body.getMovieId() + ", Crazy credit Id:" + body.getCrazyCreditId()))
            .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }

    @Override
    public Flux<CrazyCredit> getCrazyCredits(int movieId) {
        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        return repository.findByMovieId(movieId)
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public void deleteCrazyCredits(int movieId) {
    	if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
        LOG.debug("deleteCrazyCredits: tries to delete crazy credits for the movie with movieId: {}", movieId);
        repository.deleteAll(repository.findByMovieId(movieId)).block();
    }
}
