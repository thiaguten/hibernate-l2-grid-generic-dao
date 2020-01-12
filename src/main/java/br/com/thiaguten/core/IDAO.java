package br.com.thiaguten.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Interface that defines basic CRUD operations.
 *
 * @param <T>  the type of the persistent class
 * @param <ID> the type of the identifier
 * @author Thiago Gutenberg Carvalho da Costa
 */
public interface IDAO<ID extends Serializable, T extends Persistable<ID>> {

    /**
     * Get entity manager.
     *
     * @return entity manager instance
     */
    EntityManager getEntityManager();

    /**
     * Get entity manager factory.
     *
     * @return entity manager factory instance
     */
    EntityManagerFactory getEntityManagerFactory();

    /**
     * Get a persistence class.
     *
     * @return the persistence class
     */
    Class<T> getPersistenceClass();

    /**
     * Get an the identifier class.
     *
     * @return the identifier class
     */
    Class<ID> getIdentifierClass();

    /**
     * Create or Update an entity based on ID.
     *
     * @param entity entity to be created/updated
     * @return the created entity
     */
    T saveOrUpdate(final T entity);

    /**
     * Create or Update entities based on ID.
     *
     * @param entities  entities to be created/updated
     * @param batchSize the size of batch
     * @return entities created
     */
    List<T> saveOrUpdateInBatch(final List<T> entities, int batchSize);

    /**
     * Read an entity by its identifier.
     *
     * @param id entity identifier to be read
     * @return the entity
     */
    T findById(final ID id);

    /**
     * Delete an entity.
     *
     * @param entity entity to be deleted
     */
    void delete(final T entity);

    /**
     * Delete an entity by its identifier.
     *
     * @param id entity identifier to be deleted
     */
    void deleteById(final ID id);

    /**
     * Count all entities.
     *
     * @return the number of entities
     */
    Long countAll();

    /**
     * Count all entities.
     *
     * @param cacheable enable query cache
     * @return the number of entities
     */
    Long countAll(boolean cacheable);

    /**
     * Count by query and parameters.
     *
     * @param <R>         the type of the number class
     * @param resultClass the number class
     * @param query       the query string
     * @param params      the query positional parameters
     * @return the count of entities
     */
    <R extends Number> R countByQuery(Class<R> resultClass, String query, Object... params);

    /**
     * Count by query and parameters.
     *
     * @param <R>         the type of the number class
     * @param resultClass the number class
     * @param cacheable   enable query cache
     * @param query       the query string
     * @param params      the query positional parameters
     * @return the count of entities
     */
    <R extends Number> R countByQuery(Class<R> resultClass, boolean cacheable, String query, Object... params);

    /**
     * Count by query and parameters.
     *
     * @param <R>         the type of the number class
     * @param resultClass the number class
     * @param query       the query string
     * @param params      the query parameters
     * @return the count of entities
     */
    <R extends Number> R countByQueryAndNamedParams(Class<R> resultClass, String query, Map<String, ?> params);

    /**
     * Count by query and parameters.
     *
     * @param <R>         the type of the number class
     * @param resultClass the number class
     * @param cacheable   enable query cache
     * @param query       the query string
     * @param params      the query named parameters
     * @return the count of entities
     */
    <R extends Number> R countByQueryAndNamedParams(Class<R> resultClass, boolean cacheable, String query, Map<String, ?> params);

    /**
     * Load all entities.
     *
     * @return the list of entities
     */
    List<T> findAll();

    /**
     * Load entities.
     *
     * @param firstResult the value of first result
     * @param maxResults  the value of max result
     * @return the list of entities
     */
    List<T> findAll(final int firstResult, final int maxResults);

    /**
     * Load entities.
     *
     * @param cacheable   enable query cache
     * @param firstResult the value of first result
     * @param maxResults  the value of max result
     * @return the list of entities
     */
    List<T> findAll(boolean cacheable, int firstResult, int maxResults);

    /**
     * Get the largest value by id attribute name.
     *
     * @return the maximum value in a set
     */
    ID maxById();

    /**
     * Get the largest value by id attribute name.
     *
     * @return the maximum value in a set
     */
    ID maxById(String idAttributeName);

    /**
     * Get the largest value by attribute name.
     *
     * @return the maximum value in a set
     */
    <R> R max(String attributeName, Class<R> resultClass);

    /**
     * Get the largest long value by id attribute name.
     *
     * @return the long maximum value in a set
     */
    Long maxIdAsLong();

    /**
     * Get the largest long value by id attribute name.
     *
     * @param idAttributeName long id attribute name
     * @return @return the long maximum value in a set
     */
    Long maxIdAsLong(String idAttributeName);

    /**
     * Find by query.
     *
     * @param query  the query string
     * @param params the query string positional parameters
     * @return the list of entities
     */
    List<T> findByQuery(String query, Object... params);

    /**
     * Find by query.
     *
     * @param cacheable enable query cache
     * @param query     the query string
     * @param params    the query string positional parameters
     * @return the list of entities
     */
    List<T> findByQuery(boolean cacheable, String query, Object... params);

    /**
     * Find by query.
     *
     * @param cacheable   enable query cache
     * @param firstResult the value of first result
     * @param maxResults  the value of max result
     * @param query       the query string
     * @param params      the query string positional parameters
     * @return the list of entities
     */
    List<T> findByQuery(boolean cacheable, int firstResult, int maxResults, String query, Object... params);

    /**
     * Find by query and parameters.
     *
     * @param query  the query string
     * @param params the query string parameters
     * @return the list of entities
     */
    List<T> findByQueryAndNamedParams(String query, Map<String, ?> params);

    /**
     * Find by query and parameters.
     *
     * @param cacheable enable query cache
     * @param query     the query string
     * @param params    the query string parameters
     * @return the list of entities
     */
    List<T> findByQueryAndNamedParams(boolean cacheable, String query, Map<String, ?> params);

    /**
     * Find by query and parameters.
     *
     * @param cacheable   enable query cache
     * @param firstResult the value of first result
     * @param maxResults  the value of max result
     * @param query       the query string
     * @param params      the query string parameters
     * @return the list of entities
     */
    List<T> findByQueryAndNamedParams(boolean cacheable, int firstResult, int maxResults, String query, Map<String, ?> params);

}
