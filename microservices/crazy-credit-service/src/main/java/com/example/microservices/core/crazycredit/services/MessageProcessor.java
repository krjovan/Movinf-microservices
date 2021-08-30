package com.example.microservices.core.crazycredit.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.api.core.crazycredit.CrazyCreditService;
import com.example.api.event.Event;
import com.example.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final CrazyCreditService crazyCreditService;

    @Autowired
    public MessageProcessor(CrazyCreditService crazyCreditService) {
        this.crazyCreditService = crazyCreditService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, CrazyCredit> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
        	CrazyCredit crazyCredit = event.getData();
            LOG.info("Create crazy credit with ID: {}/{}", crazyCredit.getMovieId(), crazyCredit.getCrazyCreditId());
            crazyCreditService.createCrazyCredit(crazyCredit);
            break;

        case DELETE:
            int movieId = event.getKey();
            LOG.info("Delete crazy credit with MovieID: {}", movieId);
            crazyCreditService.deleteCrazyCredits(movieId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
