package com.example.microservices.core.crazycredit.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CrazyCreditRepository extends CrudRepository<CrazyCreditEntity, String> {
    List<CrazyCreditEntity> findByMovieId(int movieId);
}
