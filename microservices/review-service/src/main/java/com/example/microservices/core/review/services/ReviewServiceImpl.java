package com.example.microservices.core.review.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import api.core.review.Review;
import api.core.review.ReviewService;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReviews(int movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        if (movieId == 213) {
            LOG.debug("No reviews found for movieId: {}", movieId);
            return  new ArrayList<>();
        }

        List<Review> list = new ArrayList<>();
        list.add(new Review(movieId, 1, Date.valueOf("2021-08-13"), "Title 1", "Content 1", 1, serviceUtil.getServiceAddress()));
        list.add(new Review(movieId, 2, Date.valueOf("2021-08-13"), "Title 2", "Content 2", 2, serviceUtil.getServiceAddress()));
        list.add(new Review(movieId, 3, Date.valueOf("2021-08-13"), "Title 3", "Content 3", 3, serviceUtil.getServiceAddress()));

        LOG.debug("/reviews response size: {}", list.size());

        return list;
    }
}
