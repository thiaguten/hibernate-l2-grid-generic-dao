package br.com.thiaguten.sequence;

import br.com.thiaguten.core.AbstractIDSupplier;
import br.com.thiaguten.dao.PostDAO;
import org.apache.ignite.Ignite;

/**
 * Thread-Safe - Distributed Post id supplier.
 *
 * @author Thiago Gutenberg Carvalho da Costa
 */
public class PostIDSupplier extends AbstractIDSupplier<PostDAO> {

  public PostIDSupplier(Ignite ignite, PostDAO postDAO) {
    super(ignite, postDAO);
  }

}
