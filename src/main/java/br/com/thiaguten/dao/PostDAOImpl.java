package br.com.thiaguten.dao;

import br.com.thiaguten.core.AbstractDAO;
import br.com.thiaguten.model.Post;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import org.hibernate.criterion.MatchMode;
import org.hibernate.jpa.QueryHints;

public class PostDAOImpl extends AbstractDAO<Long, Post> implements PostDAO {

  @Override
  public List<Post> findByTitle(String title) {
    boolean cacheable = true;
    String likePattern = MatchMode.ANYWHERE.toMatchString(title.toLowerCase());

//    // Named Parameters (:name)
//    String jpql = "select distinct p "
//        + "from Post p "
//        + "join fetch p.postComments pc "
//        + "join fetch p.postDetails pd "
//        + "where lower(p.title) like :likePattern "
//        + "order by pd.createdOn";
//    return findByQueryAndNamedParams(cacheable, jpql, Collections.singletonMap("likePattern", likePattern));

//    // Ordinal Parameters (?index)
//    String jpql = "select distinct p "
//        + "from Post p "
//        + "join fetch p.postComments pc "
//        + "join fetch p.postDetails pd "
//        + "where lower(p.title) like ?1 "
//        + "order by pd.createdOn";
//    return findByQuery(cacheable, jpql, "'" + likePattern + "'");

    // Criteria Query
    EntityManager entityManager = getEntityManager();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Post> cq = cb.createQuery(getPersistenceClass());
    Root<Post> post = cq.from(getPersistenceClass());
    post.fetch("postComments");
    Join<Object, Object> postDetails = (Join<Object, Object>) post.fetch("postDetails");
    cq.select(post).distinct(true);
//    cq.where(cb.like(cb.lower(post.get("title")), cb.literal(likePattern)));
    cq.where(cb.like(cb.lower(post.get("title")), likePattern));
    cq.orderBy(cb.asc(postDetails.get("createdOn")));
    return entityManager.createQuery(cq)
        .setHint(QueryHints.HINT_CACHEABLE, cacheable)
        .getResultList();
  }

}
