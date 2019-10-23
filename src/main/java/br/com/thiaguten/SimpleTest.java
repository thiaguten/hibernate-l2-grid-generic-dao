package br.com.thiaguten;

import br.com.thiaguten.Env.ConnectionStrategy;
import br.com.thiaguten.core.PersistenceHelper;
import br.com.thiaguten.core.PersistenceUnitInfoImpl;
import br.com.thiaguten.dao.PostDAO;
import br.com.thiaguten.dao.PostDAOImpl;
import br.com.thiaguten.model.Post;
import br.com.thiaguten.model.PostComment;
import br.com.thiaguten.model.PostDetails;
import br.com.thiaguten.sequence.PostIDSupplier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTest {

  private static final Logger logger = LoggerFactory.getLogger(SimpleTest.class);

  public static void main(String[] args) {
    logger.info("Start Ignite");
    Ignite ignite = Env.startIgnite();

    logger.info("Init JPA bootstrap process");
    Map<String, Object> props = Env.createPersistenceConfig(ignite, ConnectionStrategy.DATA_SOURCE);
    PersistenceHelper.bootstrap(new PersistenceUnitInfoImpl("testUnit"), props);

    PostDAO postDAO = new PostDAOImpl();
    PostIDSupplier postIdSupplier = new PostIDSupplier(ignite, postDAO);

    AtomicInteger counter = new AtomicInteger();

    simpleCRUD(counter, postDAO, postIdSupplier);

//    Env.initHSQLDatabaseGUIManager();
  }

  private static String crudInfoPrefix(AtomicInteger counter) {
    return "current CRUD id: " + counter.incrementAndGet() + " - current thread id: " + Thread.currentThread().getId();
  }

  public static void simpleCRUD(AtomicInteger counter, PostDAO postDAO, PostIDSupplier postIdSupplier) {
    String crudInfo = Env.crudInfo(counter);

    logger.debug(">>> CREATING - " + crudInfo);
    Post post = new Post("insertTest", new PostDetails("Thiago"));
    post.setId(postIdSupplier.getNextIdAsLong());
    Post postSaved = postDAO.save(post);
    logger.debug(">>> CREATED - " + crudInfo + " - {}", postSaved);

    logger.debug(">>> UPDATING - " + crudInfo);
    postSaved.setTitle("updateTest");
    postSaved.addPostComment(new PostComment("Nice post!"));
    Post postUpdated = postDAO.update(postSaved);
    logger.debug(">>> UPDATED - " + crudInfo + " - {}", postUpdated);

    logger.debug(">>> READING - " + crudInfo);
    Post postFound = postDAO.findById(postUpdated.getId());
    logger.debug(">>> READ - " + crudInfo + " - {}", postFound);

    logger.debug(">>> SEARCHING - " + crudInfo);
    List<Post> postFounds = postDAO.findByTitle("test");
    logger.debug(">>> FOUND - " + crudInfo + " - {}", postFounds);

    logger.debug(">>> DELETING - " + crudInfo);
    postDAO.delete(postFound);
    logger.debug(">>> DELETED - " + crudInfo);
  }

}
