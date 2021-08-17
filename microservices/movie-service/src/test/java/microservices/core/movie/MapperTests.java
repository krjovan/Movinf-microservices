package microservices.core.movie;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.movie.Movie;
import com.example.microservices.core.movie.persistence.MovieEntity;
import com.example.microservices.core.movie.services.MovieMapper;

import static org.junit.Assert.*;

import java.sql.Date;

public class MapperTests {

    private MovieMapper mapper = Mappers.getMapper(MovieMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);

        Movie api = new Movie(1, "n", Date.valueOf("2021-08-12"),"s", 0, 0, 0, "sa");

        MovieEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getTitle(), entity.getTitle());
        assertEquals(api.getReleaseDate(), entity.getReleaseDate());
        assertEquals(api.getCountry(), entity.getCountry());
        assertEquals(api.getBudget(), entity.getBudget());
        assertEquals(api.getGross(), entity.getGross());
        assertEquals(api.getRuntime(), entity.getRuntime());

        Movie api2 = mapper.entityToApi(entity);

        assertEquals(api.getMovieId(), api2.getMovieId());
        assertEquals(api.getTitle(), api2.getTitle());
        assertEquals(api.getReleaseDate(), api2.getReleaseDate());
        assertEquals(api.getCountry(), api2.getCountry());
        assertEquals(api.getBudget(), api2.getBudget());
        assertEquals(api.getGross(), api2.getGross());
        assertEquals(api.getRuntime(), api2.getRuntime());
        assertNull(api2.getServiceAddress());
    }
}
