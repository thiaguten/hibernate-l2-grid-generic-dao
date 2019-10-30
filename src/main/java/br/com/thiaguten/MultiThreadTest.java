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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadTest {

  private static final Logger logger = LoggerFactory.getLogger(MultiThreadTest.class);

  public static void main(String[] args) {
    Ignite ignite = Env.startIgnite();
    Map<String, Object> props = Env.createPersistenceConfig(ignite, ConnectionStrategy.CONNECTION_PROVIDER);
    PersistenceHelper.bootstrap(new PersistenceUnitInfoImpl("testUnit"), props);
    PostDAO postDAO = new PostDAOImpl();
    PostIDSupplier postIdSupplier = new PostIDSupplier(ignite, postDAO);
    AtomicInteger counter = new AtomicInteger();

    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
      logger.error("An exception has been captured!");
      logger.error("Thread: {}", thread.getId());
      logger.error("Thread status: {}", thread.getState());
      logger.error("Exception: {}: {}", throwable.getClass().getName(), throwable.getMessage());
      logger.error("Stack Trace:", throwable);
    });

    int limit = 50;

    Supplier<Runnable> taskSupplier = () -> () -> {
      try {
//        TimeUnit.MILLISECONDS.sleep(500);
        Test.simpleCRUD(counter, postDAO, postIdSupplier);
//        Test.batchCRUD(counter, postDAO, postIdSupplier);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
    Supplier<Thread> threadSupplier = () -> new Thread(taskSupplier.get());

    Stream.generate(threadSupplier).limit(limit).parallel().forEach(Thread::start);

//    ExecutorService executorService = Executors.newCachedThreadPool();
//    Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
//    Stream.generate(taskSupplier).limit(limit).forEach(executorService::execute);

//    Env.initHSQLDatabaseGUIManager();
  }

}
