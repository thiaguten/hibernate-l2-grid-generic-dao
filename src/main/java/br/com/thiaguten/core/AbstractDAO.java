package br.com.thiaguten.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.jpa.QueryHints;
import org.hibernate.transform.ResultTransformer;

/**
 * Abstract generic DAO class that makes delegation calls features implemented
 * by a specific persistence provider.
 *
 * @param <T>  the type of the persistent class
 * @param <ID> the type of the identifier
 * 
 * @author Thiago Gutenberg Carvalho da Costa
 */
public abstract class AbstractDAO<ID extends Serializable, T extends Persistable<ID>> implements IDAO<ID, T> {

	private final Class<T> persistenceClass;
	private final Class<ID> identifierClass;
	private final EntityManagerFactory entityManagerFactory;
	private final ThreadLocal<EntityManager> threadLocalEntityManager;

	/**
	 * Construct a AbstractDAO.
	 */
	@SuppressWarnings("unchecked")
	public AbstractDAO() {
		java.lang.reflect.ParameterizedType genericSuperClass = (java.lang.reflect.ParameterizedType) getClass()
				.getGenericSuperclass();
		this.identifierClass = (Class<ID>) genericSuperClass.getActualTypeArguments()[0];
		this.persistenceClass = (Class<T>) genericSuperClass.getActualTypeArguments()[1];
		this.entityManagerFactory = PersistenceHelper.getEntityManagerFactorySingletonInstance();
		this.threadLocalEntityManager = new ThreadLocal<>();

		// Add JVM shutdown hook to close resource and avoid memory leaks.
		Runtime.getRuntime().addShutdownHook(new Thread(this::closeEntityManager));
	}
	
	// methods for transactional control

	public EntityTransaction getTransaction() {
		return getEntityManager().getTransaction();
	}

	public boolean isTransactionActive() {
		return getTransaction().isActive();
	}

	public void beginTransaction() {
		if (!isTransactionActive()) {
			getTransaction().begin();
		}
	}

	public void commitTransaction() {
		if (isTransactionActive()) {
			getTransaction().commit();
		}
	}

	public void rollbackTransaction() {
		if (isTransactionActive()) {
			getTransaction().rollback();
		}
	}

	public final void closeEntityManager() {
		EntityManager entityManager = getEntityManager();
		if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
			threadLocalEntityManager.set(null);
		}
	}

	/**
	 * Get session factory.
	 * 
	 * @return session factory instance
	 */
	public final SessionFactory getSessionFactory() {
		return getEntityManagerFactory().unwrap(SessionFactory.class);
	}

	/**
	 * Get session.
	 *
	 * @return session instance
	 */
	public final Session getSession() {
		return getEntityManager().unwrap(Session.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final EntityManager getEntityManager() {
		EntityManager entityManager = threadLocalEntityManager.get();
		if (null == entityManager) {
			entityManager = getEntityManagerFactory().createEntityManager();
			threadLocalEntityManager.set(entityManager);
		}
		return entityManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Class<T> getPersistenceClass() {
		return persistenceClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Class<ID> getIdentifierClass() {
		return identifierClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T save(T entity) {
		EntityManager entityManager = getEntityManager();
		try {
			beginTransaction();

			ID id = entity.getId();
			if (id != null) {
				entity = entityManager.merge(entity);
			} else {
				entityManager.persist(entity);
			}

			commitTransaction();
			return entity;
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T findById(ID id) {
		EntityManager entityManager = getEntityManager();
		return entityManager.find(persistenceClass, id);
	}

//	public T findById(ID id, String graphName) {
//		return findById(id, false, graphName);
//	}
//
//	@SuppressWarnings("unchecked")
//	public T findById(ID id, boolean cacheable, String graphName) {
//		EntityManager entityManager = getEntityManager();
//		EntityGraph<T> entityGraph = (EntityGraph<T>) entityManager.createEntityGraph(graphName);
//		Map<String, Object> hints = new HashMap<>();
//		hints.put(QueryHints.HINT_FETCHGRAPH, entityGraph);
//		hints.put(QueryHints.HINT_CACHEABLE, cacheable);
//		return entityManager.find(persistenceClass, id, hints);
//	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T update(T entity) {
		return save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(T entity) {
		deleteByEntityOrId(persistenceClass, entity, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(ID id) {
		deleteByEntityOrId(persistenceClass, null, id);
	}

	/**
	 * Delete an entity by its identifier.
	 *
	 * @param <ID>        the type of the identifier
	 * @param <T>         the type of the entity
	 * @param entityClazz the entity class
	 * @param entity      the type of the entity
	 * @param id          the entity identifier
	 */
	public void deleteByEntityOrId(final Class<T> entityClazz, final T entity, final ID id) {
		EntityManager entityManager = getEntityManager();
		try {
			beginTransaction();

			if (id == null && (entity == null || entity.getId() == null)) {
				throw new PersistenceException("Could not delete. ID is null.");
			}

			ID _id = id;
			if (_id == null) {
				_id = entity.getId();
			}

			T t = entityManager.getReference(entityClazz, _id);
			entityManager.remove(t);

			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long countAll() {
		return countAll(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long countAll(boolean cacheable) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(persistenceClass)));
		Long countResult = entityManager.createQuery(criteriaQuery)
				.setHint(QueryHints.HINT_CACHEABLE, cacheable)
				.getSingleResult();
		return null == countResult ? 0L : countResult;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R extends Number> R countByQueryAndNamedParams(Class<R> resultClass, String query, Map<String, ?> params) {
		EntityManager entityManager = getEntityManager();
		TypedQuery<R> typedQuery = entityManager.createQuery(query, resultClass);
		if (params != null) {
			params.forEach(typedQuery::setParameter);
		}
		return typedQuery.getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> findAll() {
		return findAll(-1, -1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> findAll(int firstResult, int maxResults) {
		return findAll(false, firstResult, maxResults);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> findAll(boolean cacheable, int firstResult, int maxResults) {
		EntityManager entityManager = getEntityManager();
		CriteriaQuery<T> cq = entityManager.getCriteriaBuilder().createQuery(persistenceClass);
		TypedQuery<T> createQuery = entityManager.createQuery(cq.select(cq.from(persistenceClass)));
		return queryRange(createQuery, firstResult, maxResults)
				.setHint(QueryHints.HINT_CACHEABLE, cacheable)
				.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ID maxById() {
		return maxById("id");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ID maxById(String idAttributeName) {
		return max(idAttributeName, identifierClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R> R max(String attributeName, Class<R> attributeClass) {
		if ((null == attributeName || attributeName.trim().isEmpty()) || null == attributeClass) {
			throw new PersistenceException("attributeName/attributeClass must not be null or empty");
		}
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<R> cq = cb.createQuery(attributeClass);
		Root<T> entity = cq.from(persistenceClass);
		cq.select((Selection<? extends R>) cb.max(entity.get(attributeName)));
		return entityManager.createQuery(cq).getSingleResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long maxIdAsLong() {
		return maxIdAsLong("id");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long maxIdAsLong(String idAttributeName) {
		Long maxResult = max(idAttributeName, Long.class);
		return null == maxResult ? 0L : maxResult;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> findByQuery(boolean cacheable, String query, Object... params) {
		return findByQuery(cacheable, -1,  -1, query, params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> findByQuery(boolean cacheable, int firstResult, int maxResults, String query, Object... params) {
		EntityManager entityManager = getEntityManager();
		TypedQuery<T> typedQuery = entityManager.createQuery(query, persistenceClass);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				typedQuery.setParameter(i + 1, params[i]); // JPQL Positional Parameters starts from 1
			}
		}
		return queryRange(typedQuery, firstResult, maxResults)
				.setHint(QueryHints.HINT_CACHEABLE, cacheable)
				.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> findByQueryAndNamedParams(boolean cacheable, String query, Map<String, ?> params) {
		EntityManager entityManager = getEntityManager();
		TypedQuery<T> typedQuery = entityManager.createQuery(query, persistenceClass);
		if (params != null) {
			params.forEach(typedQuery::setParameter);
		}
		return typedQuery.setHint(QueryHints.HINT_CACHEABLE, cacheable).getResultList();
	}

	public TypedQuery<T> queryRange(TypedQuery<T> query, int firstResult, int maxResults) {
		if (query != null) {
			if (maxResults >= 0) {
				query.setMaxResults(maxResults);
			}
			if (firstResult >= 0) {
				query.setFirstResult(firstResult);
			}
		}
		return query;
	}

	// HIBERNATE CRITERION API - TODO Migrate to JPA CRITERIA API!

	/**
	 * Find by criteria.
	 *
	 * @param criterions the criterions
	 * @param <T>        the type of the entity
	 * @return the list of entities
	 */
	public List<T> findByCriteria(List<Criterion> criterions) {
		return findByCriteria(-1, -1, criterions);
	}

	/**
	 * Find by criteria.
	 *
	 * @param criterions  the criterions
	 * @param firstResult first result
	 * @param maxResults  max result
	 * @param <T>         the type of the entity
	 * @return the list of entities
	 */
	public List<T> findByCriteria(int firstResult, int maxResults, List<Criterion> criterions) {
		return findByCriteria(false, firstResult, maxResults, criterions);
	}

	/**
	 * Find by criteria.
	 *
	 * @param entityClazz the entity class
	 * @param criterions  the criterions
	 * @param cacheable   cacheable
	 * @param firstResult first result
	 * @param maxResults  max result
	 * @param <ID>        the type of the identifier
	 * @param <T>         the type of the entity
	 * @return the list of entities
	 */
	@SuppressWarnings("unchecked")
	public List<T> findByCriteria(boolean cacheable, int firstResult, int maxResults, List<Criterion> criterions) {
		Criteria criteria = getSession().createCriteria(persistenceClass);
		if (criterions != null) {
			for (Criterion c : criterions) {
				criteria.add(c);
			}
		}
		return criteriaRange(criteria, firstResult, maxResults).setCacheable(cacheable).list();
	}

	public <N extends Number> N countByCriteria(Class<N> resultClass, boolean cacheable, List<Criterion> criterions) {
		return countByCriteria(resultClass, cacheable, Criteria.DISTINCT_ROOT_ENTITY, criterions);
	}

	@SuppressWarnings("unchecked")
	public <N extends Number> N countByCriteria(Class<N> resultClass, boolean cacheable,
			ResultTransformer resultTransformer, List<Criterion> criterions) {
		Criteria criteria = getSession().createCriteria(persistenceClass);
		criteria.setProjection(Projections.rowCount());
		criteria.setCacheable(cacheable);
		if (criterions != null) {
			for (Criterion c : criterions) {
				criteria.add(c);
			}
		}
		return (N) criteria.setResultTransformer(resultTransformer).uniqueResult();
	}

	public Criteria criteriaRange(Criteria criteria, int firstResult, int maxResults) {
		if (criteria != null) {
			if (maxResults >= 0) {
				criteria.setMaxResults(maxResults);
			}
			if (firstResult >= 0) {
				criteria.setFirstResult(firstResult);
			}
		}
		return criteria;
	}
}
