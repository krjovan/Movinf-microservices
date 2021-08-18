package com.example.microservices.core.trivia.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.example.api.core.trivia.Trivia;
import com.example.microservices.core.trivia.persistence.TriviaEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TriviaMapper {

    @Mappings({
        @Mapping(target = "publishDate", source="entity.publishDate"),
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Trivia entityToApi(TriviaEntity entity);

    @Mappings({
        @Mapping(target = "publishDate", source="api.publishDate"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    TriviaEntity apiToEntity(Trivia api);

    List<Trivia> entityListToApiList(List<TriviaEntity> entity);
    List<TriviaEntity> apiListToEntityList(List<Trivia> api);
}