package com.example.microservices.core.review.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.persistence.ReviewRepository;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import com.example.api.core.review.Review;
import com.example.api.core.review.ReviewService;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

import java.util.List;
import java.util.function.Supplier;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;
    
    private final Scheduler scheduler;
    
    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(Scheduler scheduler, ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil) {
    	this.scheduler = scheduler;
    	this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }
    
    @Override
    public Review createReview(Review body) {
    	
    	if (body.getMovieId() < 1) throw new InvalidInputException("Invalid movieId: " + body.getMovieId());
    	
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity: {}/{}", body.getMovieId(), body.getReviewId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Movie Id: " + body.getMovieId() + ", Review Id:" + body.getReviewId());
        }
    }
    
    @Override
    public Flux<Review> getReviews(int movieId) {
        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
        
        LOG.info("Will get reviews for movie with id={}", movieId);

        return asyncFlux(() -> Flux.fromIterable(getByMovieId(movieId))).log(null, FINE);
    }
    
    protected List<Review> getByMovieId(int movieId ) {
        List<ReviewEntity> entityList = repository.findByMovieId(movieId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getReviews: response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteReviews(int movieId) {
    	if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
    	
        LOG.debug("deleteReviews: tries to delete reviews for the movie with movieId: {}", movieId);
        repository.deleteAll(repository.findByMovieId(movieId));
    }
    
    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}
