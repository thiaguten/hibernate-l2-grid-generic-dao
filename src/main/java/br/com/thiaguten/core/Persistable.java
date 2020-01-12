package br.com.thiaguten.core;

import java.io.Serializable;

/**
 * Defines a class as persistable by an identifier.
 *
 * @param <ID> the type of the identifier
 * @author Thiago Gutenberg Carvalho da Costa
 */
public interface Persistable<ID extends Serializable> extends Serializable {

    /**
     * Get an identifier.
     *
     * @return the identifier
     */
    ID getId();

    /**
     * Set an identifier
     *
     * @param id the identifier
     */
    void setId(ID id);

    /**
     * Checks if this instance has a valid id.
     *
     * @return true if this instance has id, otherwise false.
     */
    default boolean hasID() {
        return this.getId() != null;
    }
}
