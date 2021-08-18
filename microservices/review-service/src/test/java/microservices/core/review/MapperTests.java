package microservices.core.review;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.review.Review;
import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.services.ReviewMapper;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MapperTests {

    private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);


    @Test
    public void mapperTests() {
        assertNotNull(mapper);

        Review api = new Review(1, 2, Date.valueOf("2021-08-12"), "Some title", "Some content", 0, "adr");

        ReviewEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getPublishDate(), entity.getPublishDate());
        assertEquals(api.getTitle(), entity.getTitle());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.getRating(), entity.getRating());

        Review api2 = mapper.entityToApi(entity);
        
        assertEquals(api.getMovieId(), api2.getMovieId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getPublishDate(), api2.getPublishDate());
        assertEquals(api.getTitle(), api2.getTitle());
        assertEquals(api.getContent(), api2.getContent());
        assertEquals(api.getRating(), api2.getRating());
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {

        assertNotNull(mapper);

        Review api = new Review(1, 2, Date.valueOf("2021-08-12"), "Some title", "Some content", 0, "adr");
        List<Review> apiList = Collections.singletonList(api);

        List<ReviewEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        ReviewEntity entity = entityList.get(0);
        
        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getPublishDate(), entity.getPublishDate());
        assertEquals(api.getTitle(), entity.getTitle());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.getRating(), entity.getRating());

        List<Review> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Review api2 = api2List.get(0);

        assertEquals(api.getMovieId(), api2.getMovieId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getPublishDate(), api2.getPublishDate());
        assertEquals(api.getTitle(), api2.getTitle());
        assertEquals(api.getContent(), api2.getContent());
        assertEquals(api.getRating(), api2.getRating());
        assertNull(api2.getServiceAddress());
    }
}