package com.example.microservices.core.crazycredit;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditEntity;
import com.example.microservices.core.crazycredit.services.CrazyCreditMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MapperTests {

    private CrazyCreditMapper mapper = Mappers.getMapper(CrazyCreditMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);

        CrazyCredit api = new CrazyCredit(1, 2, "Some content", false, "adr");

        CrazyCreditEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getCrazyCreditId(), entity.getCrazyCreditId());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.isSpoiler(), entity.isSpoiler());

        CrazyCredit api2 = mapper.entityToApi(entity);
        
        assertEquals(api.getMovieId(), api2.getMovieId());
        assertEquals(api.getCrazyCreditId(), api2.getCrazyCreditId());
        assertEquals(api.getContent(), api2.getContent());
        assertEquals(api.isSpoiler(), api2.isSpoiler());
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {
        assertNotNull(mapper);

        CrazyCredit api = new CrazyCredit(1, 2, "Some content", false, "adr");
        List<CrazyCredit> apiList = Collections.singletonList(api);

        List<CrazyCreditEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        CrazyCreditEntity entity = entityList.get(0);

        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getCrazyCreditId(), entity.getCrazyCreditId());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.isSpoiler(), entity.isSpoiler());

        List<CrazyCredit> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        CrazyCredit api2 = api2List.get(0);

        assertEquals(api.getMovieId(), api2.getMovieId());
        assertEquals(api.getCrazyCreditId(), api2.getCrazyCreditId());
        assertEquals(api.getContent(), api2.getContent());
        assertEquals(api.isSpoiler(), api2.isSpoiler());
        assertNull(api2.getServiceAddress());
    }
}
