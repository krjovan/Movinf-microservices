package com.example.microservices.core.movie.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.core.movie.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

import com.example.microservices.core.movie.persistence.MovieEntity;
import com.example.microservices.core.movie.persistence.MovieRepository;

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
        try {
            MovieEntity entity = mapper.apiToEntity(body);
            MovieEntity newEntity = repository.save(entity);

            LOG.debug("createMovie: entity created for movieId: {}", body.getMovieId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Movie Id: " + body.getMovieId());
        }
    }

    @Override
    public Movie getMovie(int movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        MovieEntity entity = repository.findByMovieId(movieId)
                .orElseThrow(() -> new NotFoundException("No movie found for movieId: " + movieId));

        Movie response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOG.debug("getMovie: found movieId: {}", response.getMovieId());
        
        return response;
    }
    
    @Override
    public void deleteMovie(int movieId) {
        LOG.debug("deleteMovie: tries to delete an entity with movieId: {}", movieId);
        repository.findByMovieId(movieId).ifPresent(e -> repository.delete(e));
    }
}