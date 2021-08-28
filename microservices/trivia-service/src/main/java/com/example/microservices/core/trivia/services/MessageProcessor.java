package com.example.microservices.core.trivia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import com.example.api.core.trivia.Trivia;
import com.example.api.core.trivia.TriviaService;
import com.example.api.event.Event;
import com.example.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final TriviaService triviaService;

    @Autowired
    public MessageProcessor(TriviaService triviaService) {
        this.triviaService = triviaService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Trivia> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
        	Trivia trivia = event.getData();
            LOG.info("Create trivia with ID: {}/{}", trivia.getMovieId(), trivia.getTriviaId());
            triviaService.createTrivia(trivia);
            break;

        case DELETE:
            int movieId = event.getKey();
            LOG.info("Delete trivia with MovieId: {}", movieId);
            triviaService.deleteTrivia(movieId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
