package br.com.thiaguten.dao;

import br.com.thiaguten.core.AbstractDAO;
import br.com.thiaguten.model.Post;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.criterion.MatchMode;
import org.hibernate.jpa.QueryHints;

public class PostDAOImpl extends AbstractDAO<Long, Post> implements PostDAO {

  @Override
  public List<Post> findByTitle(String title) {
    EntityManager entityManager = getEntityManager();
    String likePattern = MatchMode.ANYWHERE.toMatchString(title.toLowerCase());
//    return findByQuery(true, "FROM Post p JOIN FETCH p.postComments pc WHERE LOWER(p.title) like '" + likePattern + "'");

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Post> query = cb.createQuery(getPersistenceClass());
    Root<Post> from = query.from(getPersistenceClass());
    from.fetch("postComments");

//    EntityType<Post> entityType = entityManager.getMetamodel().entity(getPersistenceClass());
//    Path<String> titleExpr = from.get(entityType.getSingularAttribute("title", String.class));
//    Expression<String> titleExpr = from.get("title").as(String.class);
    Expression<String> titleExpr = from.get("title");

    Predicate wherePredicate = cb.like(cb.lower(titleExpr), likePattern);
    return entityManager
        .createQuery(query.select(from).where(wherePredicate))
        .setHint(QueryHints.HINT_CACHEABLE, "true")
        .getResultList();
  }

}
