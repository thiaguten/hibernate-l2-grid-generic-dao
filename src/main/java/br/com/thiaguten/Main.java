package br.com.thiaguten;

import static org.hibernate.cfg.AvailableSettings.CACHE_REGION_FACTORY;
import static org.hibernate.cfg.AvailableSettings.CONNECTION_PROVIDER;
import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.FORMAT_SQL;
import static org.hibernate.cfg.AvailableSettings.GENERATE_STATISTICS;
import static org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.AvailableSettings.SHOW_SQL;
import static org.hibernate.cfg.AvailableSettings.USE_QUERY_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_SECOND_LEVEL_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_STRUCTURED_CACHE;

import br.com.thiaguten.core.PersistenceHelper;
import br.com.thiaguten.core.PersistenceUnitInfoImpl;
import br.com.thiaguten.dao.PostDAO;
import br.com.thiaguten.dao.PostDAOImpl;
import br.com.thiaguten.model.Post;
import br.com.thiaguten.model.PostComment;
import br.com.thiaguten.model.PostDetails;
import br.com.thiaguten.sequence.PostIDSupplier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    Ignite ignite = Ignition.start("hibernate-l2-grid.xml");
    Runtime.getRuntime().addShutdownHook(new Thread(ignite::close));

    String url = "jdbc:hsqldb:mem:testdb;shutdown=true";
    String user = "sa";
    String password = "";

    Map<String, Object> props = new HashMap<>();
    props.put(HBM2DDL_AUTO, "update");
    props.put(DIALECT, "org.hibernate.dialect.HSQLDialect");
    props.put(SHOW_SQL, "true");
    props.put(FORMAT_SQL, "true");
    props.put(GENERATE_STATISTICS, "true");
    props.put("hibernate.hikari.poolName", "testHikariCP");
    props.put("hibernate.hikari.dataSourceClassName", "org.hsqldb.jdbc.JDBCDataSource");
    props.put("hibernate.hikari.dataSource.url", url);
    props.put("hibernate.hikari.dataSource.user", user);
    props.put("hibernate.hikari.dataSource.password", password);
    props.put("hibernate.hikari.maximumPoolSize", "10");
    props.put("hibernate.hikari.connectionTestQuery", "SELECT 1");
    props.put(CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
    props.put(USE_QUERY_CACHE, "true");
    props.put(USE_STRUCTURED_CACHE, "false");
    props.put(USE_SECOND_LEVEL_CACHE, "true");
    props.put(CACHE_REGION_FACTORY, "org.apache.ignite.cache.hibernate.HibernateRegionFactory");
    props.put("org.apache.ignite.hibernate.default_access_type", "READ_WRITE");
    props.put("org.apache.ignite.hibernate.ignite_instance_name", ignite.name());

    logger.info("Init JPA bootstrap process");
    PersistenceHelper.bootstrap(new PersistenceUnitInfoImpl("testUnit"), props);

    // create DAO instance
    PostDAO postDAO = new PostDAOImpl();
    // create id supplier instance.
    PostIDSupplier postIdSupplier = new PostIDSupplier(ignite, postDAO);

    // get next sequence id
    long postId = postIdSupplier.getNextIdAsLong();

    // save entity
    PostDetails postDetails = new PostDetails("Thiago");
    Post post = new Post("test", postDetails);
    post.setId(postId); // manually setting the id
    Post saved = postDAO.save(post);

    // update entity
    PostComment postComment = new PostComment("Nice post!");
    saved.addPostComment(postComment);
    Post updated = postDAO.update(saved);

    // search entity
    List<Post> postsFound = postDAO.findByTitle(updated.getTitle());
    postsFound.stream().map(Objects::toString).forEach(logger::info);

    // TODO I'll continue later with the other CRUD operations. I'm sleepy now...

//    org.hsqldb.util.DatabaseManagerSwing.main(new String[]{
//        "-url", url, "-user", user, "-password", password
//    });
  }

}
