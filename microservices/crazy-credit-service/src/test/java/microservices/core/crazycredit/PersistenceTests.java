package microservices.core.crazycredit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditEntity;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditRepository;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

    @Autowired
    private CrazyCreditRepository repository;

    private CrazyCreditEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll();

   		CrazyCreditEntity entity = new CrazyCreditEntity(1, 2, "Some content", false);
        savedEntity = repository.save(entity);

        assertEqualsRecommendation(entity, savedEntity);
    }


    @Test
   	public void create() {

    	CrazyCreditEntity newEntity = new CrazyCreditEntity(1, 3, "Some content", false);
        repository.save(newEntity);

        CrazyCreditEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
   	public void update() {
        savedEntity.setContent("a2");
        repository.save(savedEntity);

        CrazyCreditEntity foundEntity = repository.findById(savedEntity.getId()).get();
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
        List<CrazyCreditEntity> entityList = repository.findByMovieId(savedEntity.getMovieId());

        assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
    	CrazyCreditEntity entity = new CrazyCreditEntity(1, 2, "Some content", false);
        repository.save(entity);
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
    	CrazyCreditEntity entity1 = repository.findById(savedEntity.getId()).get();
    	CrazyCreditEntity entity2 = repository.findById(savedEntity.getId()).get();

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
        CrazyCreditEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getContent());
    }

    private void assertEqualsRecommendation(CrazyCreditEntity expectedEntity, CrazyCreditEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getMovieId(),        actualEntity.getMovieId());
        assertEquals(expectedEntity.getCrazyCreditId(), actualEntity.getCrazyCreditId());
        assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
        assertEquals(expectedEntity.isSpoiler(),           actualEntity.isSpoiler());
    }
}
