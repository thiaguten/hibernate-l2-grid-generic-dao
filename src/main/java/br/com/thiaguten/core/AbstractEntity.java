package br.com.thiaguten.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract Base Entity class that provide convenient methods and can be
 * extended by Entities classes.
 *
 * @param <ID> primary key
 * @author Thiago Gutenberg Carvalho da Costa
 */
public abstract class AbstractEntity<ID extends Serializable> implements Persistable<ID> {

    private static final long serialVersionUID = 9149914419520367894L;

    /**
     * {@inheritDoc} Overridden to implements the method behavior.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + getId() + "}";
    }

    /**
     * {@inheritDoc} Overridden to implements the method behavior.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * {@inheritDoc} Overridden to implements the method behavior.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractEntity other = (AbstractEntity) obj;
        return Objects.equals(getId(), other.getId());
    }

}
