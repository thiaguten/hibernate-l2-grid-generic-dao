package br.com.thiaguten;

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
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    Ignite ignite = Ignition.start("hibernate-l2-grid.xml");
    Runtime.getRuntime().addShutdownHook(new Thread(ignite::close));

    Map<String, Object> props = Env.createPersistenceConfig(ignite, Env.ConnectionStrategy.DATA_SOURCE);

    logger.info("Init JPA bootstrap process");
    PersistenceHelper.bootstrap(new PersistenceUnitInfoImpl("testUnit"), props);

    PostDAO postDAO = new PostDAOImpl();
    PostIDSupplier postIdSupplier = new PostIDSupplier(ignite, postDAO);

    simpleCRUD(postDAO, postIdSupplier);
//    batchCRUD(postDAO, postIdSupplier);

//    org.hsqldb.util.DatabaseManagerSwing.main(new String[]{
//        "-url", Env.url, "-user", Env.user, "-password", Env.password
//    });
  }

  private static void simpleCRUD(PostDAO postDAO, PostIDSupplier postIdSupplier) {
    logger.info("Create");
    Post post = new Post("insertTest", new PostDetails("Thiago"));
    post.setId(postIdSupplier.getNextIdAsLong());
    Post postSaved = postDAO.save(post);
    logger.info("Created: {}", postSaved);

    logger.info("Update");
    postSaved.setTitle("updateTest");
    postSaved.addPostComment(new PostComment("Nice post!"));
    Post postUpdated = postDAO.update(postSaved);
    logger.info("Updated: {}", postUpdated);

    logger.info("Read");
    Post postFound = postDAO.findById(postUpdated.getId());
    logger.info("Read: \n{}\n{}\n{}\n", postFound, postFound.getPostDetails(), postFound.getPostComments());

    logger.info("Search");
    List<Post> postFounds = postDAO.findByTitle("test");
    logger.info("Search: {}", postFounds);

    logger.info("Delete");
    postDAO.delete(postFound);
  }

  private static void batchCRUD(PostDAO postDAO, PostIDSupplier postIdSupplier) {
    logger.info("Batch create");
    int entityCount = 20;
    List<Post> posts = new ArrayList<>(entityCount);
    for (int i = 0; i < entityCount; i++) {
      Post post = new Post("insertTest" + i, new PostDetails("Thiago" + i));
      post.setId(postIdSupplier.getNextIdAsLong());
      posts.add(post);
    }
    List<Post> postsSaved = postDAO.saveInBatch(posts, Env.batchSize);
    logger.info("Batch created: {}", postsSaved);

    logger.info("Batch update");
    for (int i = 0; i < postsSaved.size(); i++) {
      Post post = postsSaved.get(i);
      Long id = post.getId();
      post.setTitle("updateTest" + id);
      for (int j = 1; j <= 4; j++) {
        post.addPostComment(new PostComment("Nice post!" + id + j));
      }
    }
    logger.info("Batch created: {}", postsSaved);
    List<Post> postsUpdated =  postDAO.updateInBatch(postsSaved, Env.batchSize);
    logger.info("Batch updated: {}", postsUpdated);

    for (int i = 0; i < postsUpdated.size(); i++) {
      Post post = postsUpdated.get(i);
      Post postFound = postDAO.findById(post.getId());
      logger.info("Read: \n{}\n{}\n{}\n", postFound, postFound.getPostDetails(), postFound.getPostComments());
    }
    // TODO I'll continue later with the other CRUD operations.
  }
}
