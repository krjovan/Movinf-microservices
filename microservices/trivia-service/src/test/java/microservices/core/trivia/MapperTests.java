package microservices.core.trivia;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.trivia.Trivia;
import com.example.microservices.core.trivia.persistence.TriviaEntity;
import com.example.microservices.core.trivia.services.TriviaMapper;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MapperTests {

    private TriviaMapper mapper = Mappers.getMapper(TriviaMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);

        Trivia api = new Trivia(1, 2, Date.valueOf("2021-08-12"), "Some contet", false, "adr");

        TriviaEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getTriviaId(), entity.getTriviaId());
        assertEquals(api.getPublishDate(), entity.getPublishDate());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.isSpoiler(), entity.isSpoiler());

        Trivia api2 = mapper.entityToApi(entity);

        assertEquals(api.getMovieId(), api2.getMovieId());
        assertEquals(api.getTriviaId(), api2.getTriviaId());
        assertEquals(api.getPublishDate(), api2.getPublishDate());
        assertEquals(api.getContent(), api2.getContent());
        assertEquals(api.isSpoiler(), api2.isSpoiler());
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {

        assertNotNull(mapper);

        Trivia api = new Trivia(1, 2, Date.valueOf("2021-08-12"), "Some contet", false, "adr");
        List<Trivia> apiList = Collections.singletonList(api);

        List<TriviaEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        TriviaEntity entity = entityList.get(0);

        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getTriviaId(), entity.getTriviaId());
        assertEquals(api.getPublishDate(), entity.getPublishDate());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.isSpoiler(), entity.isSpoiler());

        List<Trivia> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Trivia api2 = api2List.get(0);

        assertEquals(api.getMovieId(), api2.getMovieId());
        assertEquals(api.getTriviaId(), api2.getTriviaId());
        assertEquals(api.getPublishDate(), api2.getPublishDate());
        assertEquals(api.getContent(), api2.getContent());
        assertEquals(api.isSpoiler(), api2.isSpoiler());
        assertNull(api2.getServiceAddress());
    }
}
