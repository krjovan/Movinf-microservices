#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=movie-service \
--package-name=microservices.core.movie \
--groupId=microservices.core.movie \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
movie-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=trivia-service \
--package-name=microservices.core.trivia \
--groupId=microservices.core.trivia \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
trivia-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=review-service \
--package-name=microservices.core.review \
--groupId=microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
review-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=crazy-credit-service \
--package-name=microservices.core.crazy-credit \
--groupId=microservices.core.crazy-credit \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
crazy-credit-service

spring init \
--boot-version=2.3.0.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=movie-composite-service \
--package-name=microservices.composite.movie \
--groupId=microservices.composite.movie \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
movie-composite-service

cd ..