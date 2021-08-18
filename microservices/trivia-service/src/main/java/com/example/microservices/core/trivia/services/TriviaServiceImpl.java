package com.example.microservices.core.trivia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.trivia.persistence.TriviaEntity;
import com.example.microservices.core.trivia.persistence.TriviaRepository;

import com.example.api.core.trivia.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

import java.util.List;

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
        try {
            TriviaEntity entity = mapper.apiToEntity(body);
            TriviaEntity newEntity = repository.save(entity);
            LOG.debug("createTrivia: created a trivia entity: {}/{}", body.getMovieId(), body.getTriviaId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Movie Id: " + body.getMovieId() + ", Trivia Id:" + body.getTriviaId());
        }
    }
    
    @Override
    public List<Trivia> getTrivia(int movieId) {
    	if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
    	
        List<TriviaEntity> entityList = repository.findByMovieId(movieId);
        List<Trivia> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getTrivia: response size: {}", list.size());

        return list;
    }
    
    @Override
    public void deleteTrivia(int movieId) {
        LOG.debug("deleteTrivia: tries to delete trivia for the movie with movieId: {}", movieId);
        repository.deleteAll(repository.findByMovieId(movieId));
    }
}
