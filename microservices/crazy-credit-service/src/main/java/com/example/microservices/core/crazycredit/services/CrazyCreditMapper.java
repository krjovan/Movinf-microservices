package com.example.microservices.core.crazycredit.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CrazyCreditMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    CrazyCredit entityToApi(CrazyCreditEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    CrazyCreditEntity apiToEntity(CrazyCredit api);

    List<CrazyCredit> entityListToApiList(List<CrazyCreditEntity> entity);
    List<CrazyCreditEntity> apiListToEntityList(List<CrazyCredit> api);
}