package br.com.thiaguten.dao;

import br.com.thiaguten.core.IDAO;
import br.com.thiaguten.model.Post;
import java.util.List;

public interface PostDAO extends IDAO<Long, Post> {

  List<Post> findByTitle(String title);

}
