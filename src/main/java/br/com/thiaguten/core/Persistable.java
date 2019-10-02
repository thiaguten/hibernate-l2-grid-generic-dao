package br.com.thiaguten.core;

import java.io.Serializable;

/**
 * Defines a class as persistable by an identifier.
 *
 * @param <ID> the type of the identifier
 * 
 * @author Thiago Gutenberg Carvalho da Costa
 */
public interface Persistable<ID extends Serializable> extends Serializable {

	/**
	 * Get an identifier.
	 *
	 * @return the identifier
	 */
	ID getId();

}
