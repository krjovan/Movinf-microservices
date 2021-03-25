#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.2.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=movie-service \
--package-name=se.magnus.microservices.core.movie \
--groupId=se.magnus.microservices.core.movie \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
movie-service

spring init \
--boot-version=2.2.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=trivia-service \
--package-name=se.magnus.microservices.core.trivia \
--groupId=se.magnus.microservices.core.trivia \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
trivia-service

spring init \
--boot-version=2.2.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=review-service \
--package-name=se.magnus.microservices.core.review \
--groupId=se.magnus.microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

spring init \
--boot-version=2.2.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=movie-composite-service \
--package-name=se.magnus.microservices.composite.movie \
--groupId=se.magnus.microservices.composite.movie \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
movie-composite-service

cd ..