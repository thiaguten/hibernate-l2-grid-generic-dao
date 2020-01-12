package br.com.thiaguten.core;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;

/**
 * Thread-safe abstract implementation to get next id as long value.
 * <p>
 * Em um aplicativo multithread, duas ou mais threads podem precisar acessar um
 * recurso compartilhado ao mesmo tempo, resultando em comportamento inesperado.
 * Essa implementação é feita para evitar isso, tendo um comportamento
 * thread-safe, durante a busca do próximo código configuração através de uma
 * consulta ao banco de dados. Além disso, são usadas implementações atômicas do
 * Ignite para que as operações e os valores sejam visiveis globalmente entre os
 * nós do cluster.
 *
 * <p>
 * Ao invés de sincronizar o método com o modificador "synchronized" ou com um
 * "mutex object" (ReentrantLock, Semaphore, etc), utilizou-se uma estratégia
 * (lock-free) menos propensa a erros e SEM synchronized/lock que são custosos e
 * causam lentidão e dead-lock.
 *
 * @param <DAO> the persistent class dao
 * @author Thiago Gutenberg Carvalho da Costa
 */
public abstract class AbstractIDSupplier<DAO extends IDAO<?, ?>> implements IDSupplier {

    private final DAO dao;
    private final IgniteAtomicLong igniteAtomicLong;

    public AbstractIDSupplier(Ignite ignite, DAO dao) {
        this(ignite, dao, "IDSupplier");
    }

    public AbstractIDSupplier(Ignite ignite, DAO dao, String name) {
        String cacheName = Objects.requireNonNull(
                StringUtils.stripToNull(name), "name parameter must not be null/empty/whitespace");
        this.dao = Objects.requireNonNull(dao, "dao parameter must not be null");
        this.igniteAtomicLong = Objects.requireNonNull(ignite, "ignite parameter must not be null")
                .atomicLong(dao.getPersistenceClass().getSimpleName() + cacheName, calculateInitialValue(), true);

        // Add shutdown hook to close this atomic long cache when the program exits
        // closing any holding resources and to avoid memory leaks
        Runtime.getRuntime().addShutdownHook(new Thread(igniteAtomicLong::close));
    }

    protected long calculateInitialValue() {
        Long initialValue = dao.maxIdAsLong();
        return null == initialValue ? 0L : initialValue;
    }

    @Override
    public long getNextIdAsLong() {
        return igniteAtomicLong.incrementAndGet();
    }

    public long getCurrentIdAsLong() {
        return igniteAtomicLong.get();
    }

    public DAO getDao() {
        return dao;
    }

}
