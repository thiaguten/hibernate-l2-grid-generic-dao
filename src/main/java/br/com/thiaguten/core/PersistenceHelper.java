package br.com.thiaguten.core;

import com.zaxxer.hikari.proxy.ConnectionProxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.boot.spi.Bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to initialize JPA bootstrap process and manage resources.
 *
 * @author Thiago Gutenberg Carvalho da Costa
 */
public final class PersistenceHelper {

	private static final Logger logger = LoggerFactory.getLogger(PersistenceHelper.class);

	static {
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(PersistenceProviderResolverImpl.getInstance());
	}

	private EntityManagerFactory entityManagerFactory;
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private ThreadLocal<EntityManager> entityManagerHolder = new ThreadLocal<>();

	private PersistenceHelper() {
		// singleton

		// Add JVM shutdown hook to close resource and avoid memory leaks.
		Runtime.getRuntime().addShutdownHook(new Thread(this::close));
	}

	/**
	 * Initialization-on-demand holder idiom implementation.
	 *
	 * @author Thiago Gutenberg Carvalho da Costa
	 */
	private static class LazySingletonHolder {
		private static final PersistenceHelper INSTANCE = new PersistenceHelper();
	}

	/**
	 * Get PersistenceHelper instance.
	 *
	 * @return persistence helper singleton instance
	 */
	public static PersistenceHelper getInstance() {
		return LazySingletonHolder.INSTANCE;
	}

	public EntityManager createEntityManager() {
		logger.debug("Creating entity manager instance");
		return getEntityManagerFactory().createEntityManager();
	}

	/**
	 * Get entity manager.
	 *
	 * @return entity manager instance
	 */
	public EntityManager getEntityManager() {
		EntityManager entityManager = entityManagerHolder.get();
		if (null == entityManager || !entityManager.isOpen()) {
			entityManager = createEntityManager();
			entityManagerHolder.set(entityManager);
		}
		return entityManager;
	}

	/**
	 * Get entity manager factory.
	 *
	 * @return entity manager factory instance
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		if (null == entityManagerFactory || !entityManagerFactory.isOpen()) {
			throw new PersistenceException(
					"EntityManagerFactory instance is null or closed, invoke initialize method to create a new one");
		}
		return entityManagerFactory;
	}

//	/**
//	 * Get session.
//	 *
//	 * @return session instance
//	 */
//	public Session getSession() {
//		return getEntityManager().unwrap(Session.class);
//	}
//
//	/**
//	 * Get SessionFactory.
//	 *
//	 * @return session factory instance
//	 */
//	public SessionFactory getSessionFactory() {
//		return getEntityManagerFactory().unwrap(SessionFactory.class);
//	}

	/**
	 * Close an application-managed entity manager.
	 */
	public void closeEntityManager() {
		EntityManager entityManager = entityManagerHolder.get();
		if (entityManager != null && entityManager.isOpen()) {
			logger.debug("Closing entity manager instance");

			Session session = entityManager.unwrap(Session.class);
			session.doWork(connection -> {
				// do whatever you need to do with the connection
				if (connection instanceof ConnectionProxy) {
					logger.info("Closing connection proxy, releasing connection to the pool. - {}", connection);
				}
			});

			entityManager.close();
			entityManagerHolder.set(null);
		}
	}

	/**
	 * Close the factory, releasing any resources that it holds.
	 */
	public void closeEntityManagerFactory() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			logger.debug("Closing entity manager factory instance");
			entityManagerFactory.close();
		}
	}

	/**
	 * Close an application-managed entity manager and entity manager factory,
	 * releasing any resources that it holds.
	 */
	public void close() {
		closeEntityManager();
		closeEntityManagerFactory();
	}

	/**
	 * Initialize JPA bootstrap process by creating a new EntityManagerFactory and
	 * EntityManager instances.
	 *
	 * Through this initialization method the META-INF/persistence.xml file is
	 * mandatory.
	 *
	 * @param name  persistence unit Name
	 * @param props persistence unit properties
	 */
	public void initialize(String name, Map<String, Object> props) {
		logger.info("Init JPA bootstrap process");
		if (initialized.compareAndSet(false, true)) {
			logger.debug("Creating entity manager factory instance");
			entityManagerFactory = Persistence.createEntityManagerFactory(name, props);
			entityManagerHolder.set(createEntityManager());
//			entityManagerHolder = ThreadLocal.withInitial(this::createEntityManager);
		}
	}

	/**
	 * Initialize JPA bootstrap process by creating a new EntityManagerFactory and
	 * EntityManager instances.
	 *
	 * Through this initialization method the META-INF/persistence.xml file is NOT
	 * mandatory.
	 *
	 * @param info  persistence unit info
	 * @param props persistence unit properties
	 */
	public void initialize(PersistenceUnitInfo info, Map<String, Object> props) {
		logger.info("Init JPA bootstrap process");
		if (initialized.compareAndSet(false, true)) {
			logger.debug("Creating entity manager factory instance");
			ClassLoader classLoader = this.getClass().getClassLoader();
//			entityManagerFactory = PersistenceHelper.createEntityManagerFactory(info, props);
			/*
			 * Using the Hibernate initialization class directly, because I can mainly pass
			 * a PersistenceUnitInfo and also a Classloader as a parameter, different from
			 * the Persistence class of the JPA specification.
			 */
			entityManagerFactory = Bootstrap.getEntityManagerFactoryBuilder(info, props, classLoader).build();
			entityManagerHolder.set(createEntityManager());
//			entityManagerHolder = ThreadLocal.withInitial(this::createEntityManager);
		}
	}

	/**
	 * Create EntityManagerFactory of the first loaded
	 * <code>PersistenceProvider</code> Service Provider Implementations - SPI
	 * available in the runtime environment.
	 *
	 * This method was created because the javax.persistence.Persistence class does
	 * not have a static method that takes as a parameter an instance of the
	 * PersistenceUnitInfo interface.
	 *
	 * @param info  persistence unit info
	 * @param props persistence unit properties
	 * @return entity manager factory instance
	 */
	@SuppressWarnings("unused")
	private static EntityManagerFactory createEntityManagerFactory(PersistenceUnitInfo info, Map<String, Object> props) {
		EntityManagerFactory emf = null;
		List<PersistenceProvider> providers = PersistenceProviderResolverHolder
				.getPersistenceProviderResolver()
				.getPersistenceProviders();

		for (PersistenceProvider provider : providers) {
			emf = provider.createContainerEntityManagerFactory(info, props);
			if (emf != null) {
				break;
			}
		}

		if (emf == null) {
			throw new PersistenceException(
					"No Persistence provider for EntityManager named " + info.getPersistenceUnitName());
		}

		return emf;
	}

	// CONVENIENT UTILITY STATIC METHODS

	public static void bootstrap(String name, Map<String, Object> props) {
		PersistenceHelper.getInstance().initialize(name, props);
	}

	public static void bootstrap(PersistenceUnitInfo info, Map<String, Object> props) {
		PersistenceHelper.getInstance().initialize(info, props);
	}

	public static EntityManagerFactory getEntityManagerFactoryInstance() {
		// singleton instance
		return PersistenceHelper.getInstance().getEntityManagerFactory();
	}

	public static EntityManager getEntityManagerInstance() {
		// thread-safe instance
		return PersistenceHelper.getInstance().getEntityManager();
	}

//	public static SessionFactory getSessionFactoryInstance() {
//		return PersistenceHelper.getInstance().getSessionFactory();
//	}
//
//	public static Session getSessionInstance() {
//		return PersistenceHelper.getInstance().getSession();
//	}

	public static void closeEntityManagerInstance() {
		PersistenceHelper.getInstance().closeEntityManager();
	}

}
