package com.example.microservices.core.crazycredit.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.core.crazycredit.*;
import com.example.util.exceptions.*;
import com.example.util.http.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CrazyCreditServiceImpl implements CrazyCreditService {

    private static final Logger LOG = LoggerFactory.getLogger(CrazyCreditServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public CrazyCreditServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<CrazyCredit> getCrazyCredits(int movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        if (movieId == 213) {
            LOG.debug("No crazy credits found for movieId: {}", movieId);
            return  new ArrayList<>();
        }

        List<CrazyCredit> list = new ArrayList<>();
        list.add(new CrazyCredit(movieId, 1, "Content 1", false, serviceUtil.getServiceAddress()));
        list.add(new CrazyCredit(movieId, 2, "Content 2", false, serviceUtil.getServiceAddress()));
        list.add(new CrazyCredit(movieId, 3, "Content 3", false, serviceUtil.getServiceAddress()));

        LOG.debug("/crazy-credit response size: {}", list.size());

        return list;
    }
}
