package br.com.thiaguten.model;

import br.com.thiaguten.core.AbstractEntity;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "POST_COMMENT")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PostComment extends AbstractEntity<Long> {

  private static final long serialVersionUID = 4984086838635425445L;

  @Id
  @GeneratedValue
  @Column(name = "POST_COMMENT_ID", updatable = false, nullable = false)
  private Long id;

  @Column(name = "REVIEW", nullable = false)
  private String review;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "POST_ID", nullable = false)
  private Post post;

  public PostComment() {
    super();
  }

  public PostComment(String review) {
    this.review = review;
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getReview() {
    return review;
  }

  public void setReview(String review) {
    this.review = review;
  }

  public Post getPost() {
    return post;
  }

  public void setPost(Post post) {
    this.post = post;
  }

  @Override
  public String toString() {
    return "PostComment{" +
        "id=" + id +
        ", review='" + review + '\'' +
        '}';
  }
}