package br.com.thiaguten.core;

/**
 * A supplier of {@code long}-valued ID results.
 *
 * @author Thiago Gutenberg Carvalho da Costa
 */
@FunctionalInterface
public interface IDSupplier {

    long getNextIdAsLong();

}
