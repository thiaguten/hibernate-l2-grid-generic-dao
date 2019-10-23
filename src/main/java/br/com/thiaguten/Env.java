package br.com.thiaguten;

import static org.hibernate.cfg.AvailableSettings.CACHE_REGION_FACTORY;
import static org.hibernate.cfg.AvailableSettings.CONNECTION_PROVIDER;
import static org.hibernate.cfg.AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS;
import static org.hibernate.cfg.AvailableSettings.DATASOURCE;
import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.FORMAT_SQL;
import static org.hibernate.cfg.AvailableSettings.GENERATE_STATISTICS;
import static org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.AvailableSettings.ORDER_INSERTS;
import static org.hibernate.cfg.AvailableSettings.ORDER_UPDATES;
import static org.hibernate.cfg.AvailableSettings.STATEMENT_BATCH_SIZE;
import static org.hibernate.cfg.AvailableSettings.USE_QUERY_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_SECOND_LEVEL_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_STRUCTURED_CACHE;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hsqldb.jdbc.JDBCDataSource;

public class Env {

  public static final String url = "jdbc:hsqldb:mem:test;shutdown=true";
  public static final String user = "sa";
  public static final String password = "";
  public static final int batchSize = 50; //Integer.parseInt(Dialect.DEFAULT_BATCH_SIZE);

  public enum ConnectionStrategy {
    DATA_SOURCE, CONNECTION_PROVIDER
  }

  public static String crudInfo(AtomicInteger counter) {
    return "[CRUD running id: " + counter.incrementAndGet() + " - Thread current id: " + Thread.currentThread().getId() + "]";
  }

  public static void initHSQLDatabaseGUIManager() {
    org.hsqldb.util.DatabaseManagerSwing.main(new String[]{
        "-url", Env.url, "-user", Env.user, "-password", Env.password
    });
  }

  public static Ignite startIgnite() {
//    System.setProperty(org.apache.ignite.IgniteSystemProperties.IGNITE_QUIET, "false");
    System.setProperty("java.net.preferIPv4Stack", "true");
    Ignite ignite = Ignition.start("hibernate-l2-grid.xml");
    Runtime.getRuntime().addShutdownHook(new Thread(ignite::close));
    return ignite;
  }

  public static Map<String, Object> createPersistenceConfig(Ignite ignite, ConnectionStrategy connectionStrategy) {
    Map<String, Object> props = new HashMap<>();
    /*
      Enabling logging with this config the logs go to console, which makes it very difficult
      to filter them appropriately. A much better approach is to enable SQL statement logging
      using a log adapter: <logger name="org.hibernate.SQL" level="debug"/>
     */
//    props.put(SHOW_SQL, "false");
    props.put(FORMAT_SQL, "true");
    props.put(HBM2DDL_AUTO, "update");
    props.put(DIALECT, "org.hibernate.dialect.HSQLDialect");
    props.put(GENERATE_STATISTICS, "false");
    props.put(CURRENT_SESSION_CONTEXT_CLASS, "thread");
    props.put(ORDER_INSERTS, "true");
    props.put(ORDER_UPDATES, "true");
    props.put(STATEMENT_BATCH_SIZE, Integer.toString(batchSize));
    props.put(CACHE_REGION_FACTORY, "org.apache.ignite.cache.hibernate.HibernateRegionFactory");
    props.put(USE_QUERY_CACHE, "true");
    props.put(USE_STRUCTURED_CACHE, "false");
    props.put(USE_SECOND_LEVEL_CACHE, "true");
    props.put("org.apache.ignite.hibernate.default_access_type", "READ_WRITE");
    props.put("org.apache.ignite.hibernate.ignite_instance_name", ignite.name());

    if (ConnectionStrategy.CONNECTION_PROVIDER.equals(connectionStrategy)) {
      props.put(CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
      props.put("hibernate.hikari.dataSourceClassName", "org.hsqldb.jdbc.JDBCDataSource");
      props.put("hibernate.hikari.dataSource.url", url);
      props.put("hibernate.hikari.dataSource.user", user);
      props.put("hibernate.hikari.dataSource.password", password);
      props.put("hibernate.hikari.maximumPoolSize", "10");
    } else if (ConnectionStrategy.DATA_SOURCE.equals(connectionStrategy)) {
      /*
        Although Hibernate can log SQL statements by setting the proper log appender or config,
        it’s much better to delegate this responsibility to a more powerful JDBC DataSource or
        Driver proxy solution with logging capabilities.
      */
//      props.put(DATASOURCE, hikariDataSource());
      props.put(DATASOURCE, proxyDataSource());
    }

    return props;
  }

  private static JDBCDataSource dataSource() {
    JDBCDataSource ds = new JDBCDataSource();
    ds.setUrl(url);
    ds.setUser(user);
    ds.setPassword(password);
    return ds;
  }

  public static HikariDataSource hikariDataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setMaximumPoolSize(10);
    ds.setDataSource(dataSource());
    return ds;
  }

  public static ProxyDataSource proxyDataSource() {
    DefaultQueryLogEntryCreator prettyQueryLogEntryCreator = new DefaultQueryLogEntryCreator() {
      // use hibernate internal formatter to format queries
      private Formatter formatter = FormatStyle.BASIC.getFormatter();

      @Override
      protected String formatQuery(String query) {
        return this.formatter.format(query);
      }
    };
    prettyQueryLogEntryCreator.setMultiline(true);

    SLF4JQueryLoggingListener slf4JQueryLoggingListener = new SLF4JQueryLoggingListener();
    slf4JQueryLoggingListener.setQueryLogEntryCreator(prettyQueryLogEntryCreator);
    slf4JQueryLoggingListener.setLogLevel(SLF4JLogLevel.INFO);

    ProxyDataSource proxyDataSource = ProxyDataSourceBuilder
        .create(hikariDataSource())
        .listener(slf4JQueryLoggingListener)
        .multiline()
        .build();

    // JVM Shutdown hook to close resources.
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        if (proxyDataSource != null) {
          proxyDataSource.close();
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }));

    return proxyDataSource;
  }

}
