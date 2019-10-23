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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchTest {

  private static final Logger logger = LoggerFactory.getLogger(BatchTest.class);

  public static void main(String[] args) {
    logger.info("Start Ignite");
    Ignite ignite = Env.startIgnite();

    logger.info("Init JPA bootstrap process");
    Map<String, Object> props = Env.createPersistenceConfig(ignite, ConnectionStrategy.DATA_SOURCE);
    PersistenceHelper.bootstrap(new PersistenceUnitInfoImpl("testUnit"), props);

    PostDAO postDAO = new PostDAOImpl();
    PostIDSupplier postIdSupplier = new PostIDSupplier(ignite, postDAO);

    AtomicInteger counter = new AtomicInteger();

    batchCRUD(counter, postDAO, postIdSupplier);

//    Env.initHSQLDatabaseGUIManager();
  }

  public static void batchCRUD(AtomicInteger counter, PostDAO postDAO, PostIDSupplier postIdSupplier) {
    String crudInfo = Env.crudInfo(counter);

    logger.debug(">>> BATCH CREATING - " + crudInfo);
    int entityCount = 100;
    List<Post> posts = new ArrayList<>(entityCount);
    for (int i = 0; i < entityCount; i++) {
      Post post = new Post("insertTest" + i, new PostDetails("Thiago" + i));
      post.setId(postIdSupplier.getNextIdAsLong());
      posts.add(post);
    }
    List<Post> postsSaved = postDAO.saveInBatch(posts, Env.batchSize);
    logger.debug(">>> BATCH CREATED - " + crudInfo + " - {}", postsSaved);

    logger.debug(">>> BATCH UPDATING - " + crudInfo);
    for (Post post : postsSaved) {
      Long id = post.getId();
      post.setTitle("updateTest" + id);
      for (int j = 1; j <= 4; j++) {
        post.addPostComment(new PostComment("Nice post!" + id + j));
      }
    }
    List<Post> postsUpdated =  postDAO.updateInBatch(postsSaved, Env.batchSize);
    logger.debug(">>> BATCH UPDATED - " + crudInfo + " - {}", postsUpdated);

    // TODO I'll continue later with the other CRUD operations.
  }
}
