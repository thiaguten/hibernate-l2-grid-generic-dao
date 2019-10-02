package br.com.thiaguten.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.jpa.boot.spi.Bootstrap;

/**
 * Helper class to initialize JPA bootstrap process and manage resources.
 * 
 * @author Thiago Gutenberg Carvalho da Costa
 */
public final class PersistenceHelper {

	static {
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(PersistenceProviderResolverImpl.getInstance());
	}

//	private EntityManager entityManager;
	private EntityManagerFactory entityManagerFactory;
	private AtomicBoolean initialized = new AtomicBoolean(false);

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

//	/**
//	 * Get entity manager.
//	 * 
//	 * @return entity manager instance
//	 */
//	public EntityManager getEntityManager() {
//		if (entityManager != null && entityManager.isOpen()) {
//			return entityManager;
//		}
//		entityManager = getEntityManagerFactory().createEntityManager();
//		return entityManager;
//	}

	/**
	 * Get entity manager factory.
	 * 
	 * @return entity manager factory instance
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			return entityManagerFactory;
		}
		throw new PersistenceException(
				"EntityManagerFactory instance is null or closed, invoke initialize method to create a new one");
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
	 * Close an application-managed entity manager and entity manager factory,
	 * releasing any resources that it holds.
	 */
	public void close() {
//		if (entityManager != null && entityManager.isOpen()) {
//			entityManager.close();
//		}
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
		}
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
		if (initialized.compareAndSet(false, true)) {
			entityManagerFactory = Persistence.createEntityManagerFactory(name, props);
//			entityManager = entityManagerFactory.createEntityManager();
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
		if (initialized.compareAndSet(false, true)) {
			ClassLoader classLoader = this.getClass().getClassLoader();
//			entityManagerFactory = PersistenceHelper.createEntityManagerFactory(info, props);
			entityManagerFactory = Bootstrap.getEntityManagerFactoryBuilder(info, props, classLoader).build();
//			entityManager = entityManagerFactory.createEntityManager();
		}
	}

	/**
	 * Create EntityManagerFactory of the first loaded
	 * <code>PersistenceProvider</code> Service Provider Implementations - SPI
	 * available in the runtime environment.
	 * 
	 * @param info  persistence unit info
	 * @param props persistence unit properties
	 * @return entity manager factory instance
	 */
	@SuppressWarnings("unused")
	private static EntityManagerFactory createEntityManagerFactory(
			PersistenceUnitInfo info,
			Map<String, Object> props) {

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

//	public static EntityManager getEntityManagerInstance() {
//		return PersistenceHelper.getInstance().getEntityManager();
//	}

	public static EntityManagerFactory getEntityManagerFactorySingletonInstance() {
		return PersistenceHelper.getInstance().getEntityManagerFactory();
	}

//	public static Session getSessionInstance() {
//		return PersistenceHelper.getInstance().getSession();
//	}
//
//	public static SessionFactory getSessionFactoryInstance() {
//		return PersistenceHelper.getInstance().getSessionFactory();
//	}
//
//	public static EntityTransaction getTransaction() {
//		return getEntityManagerInstance().getTransaction();
//	}
//
//	public static boolean isTransactionActive() {
//		return getTransaction().isActive();
//	}
//
//	public static void beginTransaction() {
//		if (!isTransactionActive()) {
//			getTransaction().begin();
//		}
//	}
//
//	public static void commitTransaction() {
//		if (isTransactionActive()) {
//			getTransaction().commit();
//		}
//	}
//
//	public static void rollbackTransaction() {
//		if (isTransactionActive()) {
//			getTransaction().rollback();
//		}
//	}
}
