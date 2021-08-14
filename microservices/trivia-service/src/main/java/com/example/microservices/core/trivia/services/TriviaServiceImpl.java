package com.example.microservices.core.trivia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import api.core.trivia.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TriviaServiceImpl implements TriviaService {

    private static final Logger LOG = LoggerFactory.getLogger(TriviaServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public TriviaServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Trivia> getTrivia(int movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        if (movieId == 113) {
            LOG.debug("No trivia found for movieId: {}", movieId);
            return  new ArrayList<>();
        }

        List<Trivia> list = new ArrayList<>();
        list.add(new Trivia(movieId, 1, Date.valueOf("2021-08-13"), "Content 1", false, serviceUtil.getServiceAddress()));
        list.add(new Trivia(movieId, 2, Date.valueOf("2021-08-13"), "Content 2", false, serviceUtil.getServiceAddress()));
        list.add(new Trivia(movieId, 3, Date.valueOf("2021-08-13"), "Content 3", false, serviceUtil.getServiceAddress()));

        LOG.debug("/trivia response size: {}", list.size());

        return list;
    }
}
