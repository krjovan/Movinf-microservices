package microservices.core.trivia;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.microservices.core.trivia.persistence.*;

import java.sql.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

    @Autowired
    private TriviaRepository repository;

    private TriviaEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll();

        TriviaEntity entity = new TriviaEntity(1, 2, Date.valueOf("2021-08-12"), "Some contet", false);
        savedEntity = repository.save(entity);

        assertEqualsTrivia(entity, savedEntity);
    }


    @Test
   	public void create() {

    	TriviaEntity newEntity = new TriviaEntity(1, 3, Date.valueOf("2021-08-12"), "Some contet", false);
        repository.save(newEntity);

        TriviaEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsTrivia(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
   	public void update() {
        savedEntity.setContent("a2");
        repository.save(savedEntity);

        TriviaEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getContent());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByMovieId() {
        List<TriviaEntity> entityList = repository.findByMovieId(savedEntity.getMovieId());

        assertThat(entityList, hasSize(1));
        assertEqualsTrivia(savedEntity, entityList.get(0));
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
    	TriviaEntity entity = new TriviaEntity(1, 2, Date.valueOf("2021-08-12"), "Some content", false);
        repository.save(entity);
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
    	TriviaEntity entity1 = repository.findById(savedEntity.getId()).get();
    	TriviaEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setContent("a1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setContent("a2");
            repository.save(entity2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        TriviaEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getContent());
    }

    private void assertEqualsTrivia(TriviaEntity expectedEntity, TriviaEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getMovieId(),        actualEntity.getMovieId());
        assertEquals(expectedEntity.getTriviaId(), actualEntity.getTriviaId());
        assertEquals(expectedEntity.getPublishDate(),           actualEntity.getPublishDate());
        assertEquals(expectedEntity.getContent(),           actualEntity.getContent());
        assertEquals(expectedEntity.isSpoiler(),          actualEntity.isSpoiler());
    }
}