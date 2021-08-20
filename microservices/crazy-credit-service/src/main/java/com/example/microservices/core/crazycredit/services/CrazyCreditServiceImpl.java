package com.example.microservices.core.crazycredit.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditEntity;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditRepository;

import com.example.api.core.crazycredit.*;
import com.example.util.exceptions.*;
import com.example.util.http.*;

import java.util.List;

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
        try {
        	CrazyCreditEntity entity = mapper.apiToEntity(body);
        	CrazyCreditEntity newEntity = repository.save(entity);

            LOG.debug("createCrazyCredit: created a crazy credit entity: {}/{}", body.getMovieId(), body.getCrazyCreditId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Movie Id: " + body.getMovieId() + ", CrazyCredit Id:" + body.getCrazyCreditId());
        }
    }

    @Override
    public List<CrazyCredit> getCrazyCredits(int movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        List<CrazyCreditEntity> entityList = repository.findByMovieId(movieId);
        List<CrazyCredit> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getCrazyCredits: response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteCrazyCredits(int movieId) {
        LOG.debug("deleteCrazyCredits: tries to delete crazy credits for the movie with movieId: {}", movieId);
        repository.deleteAll(repository.findByMovieId(movieId));
    }
}
