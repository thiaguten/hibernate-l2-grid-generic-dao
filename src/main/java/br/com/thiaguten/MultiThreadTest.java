package br.com.thiaguten;

import br.com.thiaguten.Env.ConnectionStrategy;
import br.com.thiaguten.core.PersistenceHelper;
import br.com.thiaguten.core.PersistenceUnitInfoImpl;
import br.com.thiaguten.dao.PostDAO;
import br.com.thiaguten.dao.PostDAOImpl;
import br.com.thiaguten.sequence.PostIDSupplier;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadTest {

  private static final Logger logger = LoggerFactory.getLogger(MultiThreadTest.class);

  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
      logger.error("An exception has been captured!");
      logger.error("Thread: {}", thread.getId());
      logger.error("Thread status: {}", thread.getState());
      logger.error("Exception: {}: {}", throwable.getClass().getName(), throwable.getMessage());
      logger.error("Stack Trace:", throwable);
    });

    ExecutorService executorService = Executors.newCachedThreadPool();
    Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

    logger.info("Start Ignite");
    Ignite ignite = Env.startIgnite();

    logger.info("Init JPA bootstrap process");
    Map<String, Object> props = Env.createPersistenceConfig(ignite, ConnectionStrategy.CONNECTION_PROVIDER);
    PersistenceHelper.bootstrap(new PersistenceUnitInfoImpl("testUnit"), props);

    PostDAO postDAO = new PostDAOImpl();
    PostIDSupplier postIdSupplier = new PostIDSupplier(ignite, postDAO);
    AtomicInteger counter = new AtomicInteger();

//    Runnable task = createCRUDTask(postDAO, postIdSupplier);
//    executorService.execute(task);
//    executorService.execute(task);
//    executorService.execute(task);
//    executorService.execute(task);

    // perform crud operations pretty much simultaneously (concurrently)
    int entityCount = 10;
    Supplier<Runnable> taskSupplier = () -> createCRUDTask(counter, postDAO, postIdSupplier);
    Stream.generate(taskSupplier).limit(entityCount).forEach(executorService::execute);

//    Env.initHSQLDatabaseGUIManager();
  }

  private static Runnable createCRUDTask(AtomicInteger counter, PostDAO postDAO, PostIDSupplier postIdSupplier) {
    return () -> {
      try {
        Thread.sleep(500);
//        SimpleTest.simpleCRUD(counter, postDAO, postIdSupplier);
        BatchTest.batchCRUD(counter, postDAO, postIdSupplier);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

}
