package microservices.core.movie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.microservices.core.movie.persistence.MovieEntity;
import com.example.microservices.core.movie.persistence.MovieRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

    @Autowired
    private MovieRepository repository;

    private MovieEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll();

   		MovieEntity entity = new MovieEntity(1, "n", Date.valueOf("2021-08-12"),"s", 0, 0, 0);
        savedEntity = repository.save(entity);

        assertEqualsMovie(entity, savedEntity);
    }


    @Test
   	public void create() {
        MovieEntity newEntity = new MovieEntity(2, "n", Date.valueOf("2021-08-12"),"s", 0, 0, 0);
        repository.save(newEntity);

        MovieEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsMovie(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
   	public void update() {
        savedEntity.setTitle("n2");
        repository.save(savedEntity);

        MovieEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getTitle());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByMovieId() {
        Optional<MovieEntity> entity = repository.findByMovieId(savedEntity.getMovieId());

        assertTrue(entity.isPresent());
        assertEqualsMovie(savedEntity, entity.get());
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
    	MovieEntity entity = new MovieEntity(savedEntity.getMovieId(),"n", Date.valueOf("2021-08-12"),"s", 0, 0, 0);
        repository.save(entity);
    }

    @Test
   	public void optimisticLockError() {
        // Store the saved entity in two separate entity objects
    	MovieEntity entity1 = repository.findById(savedEntity.getId()).get();
    	MovieEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setTitle("n1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setTitle("n2");
            repository.save(entity2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        MovieEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getTitle());
    }

    @Test
    public void paging() {
        repository.deleteAll();

        List<MovieEntity> newMovies = rangeClosed(1001, 1010)
            .mapToObj(i -> new MovieEntity(i, "name " + i, Date.valueOf("2021-08-12"),"s", 0, 0, 0))
            .collect(Collectors.toList());
        repository.saveAll(newMovies);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "movieId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedMovieIds, boolean expectsNextPage) {
        Page<MovieEntity> moviePage = repository.findAll(nextPage);
        assertEquals(expectedMovieIds, moviePage.getContent().stream().map(p -> p.getMovieId()).collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, moviePage.hasNext());
        return moviePage.nextPageable();
    }

    private void assertEqualsMovie(MovieEntity expectedEntity, MovieEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getMovieId(), actualEntity.getMovieId());
        assertEquals(expectedEntity.getTitle(), actualEntity.getTitle());
        assertEquals(expectedEntity.getReleaseDate(), actualEntity.getReleaseDate());
        assertEquals(expectedEntity.getCountry(), actualEntity.getCountry());
        assertEquals(expectedEntity.getBudget(), actualEntity.getBudget());
        assertEquals(expectedEntity.getGross(), actualEntity.getGross());
        assertEquals(expectedEntity.getRuntime(), actualEntity.getRuntime());
    }
}
