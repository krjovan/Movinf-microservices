package com.example.microservices.core.movie.services;

import java.sql.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.core.movie.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

@RestController
public class MovieServiceImpl implements MovieService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public MovieServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Movie getMovie(int movieId) {
        LOG.debug("/movie returns the found movie for movieId={}", movieId);

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        if (movieId == 13) throw new NotFoundException("No movie found for movieId: " + movieId);
        
        return new Movie(movieId, "Test Title " + movieId, Date.valueOf("2021-08-13"), "Test country", 0, 0, 0, serviceUtil.getServiceAddress());
    }
}