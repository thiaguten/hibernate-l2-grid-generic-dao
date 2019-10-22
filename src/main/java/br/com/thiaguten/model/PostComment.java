package br.com.thiaguten.model;

import br.com.thiaguten.core.AbstractEntity;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "POST_COMMENT")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PostComment extends AbstractEntity<Long> {

  private static final long serialVersionUID = 4984086838635425445L;

  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_comment_sequence_generator")
  @GenericGenerator(
      name = "post_comment_sequence_generator",
      strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
      parameters = {
          @Parameter(name = "sequence_name", value = "post_comment_sequence"),
          @Parameter(name = "initial_value", value = "1"),
          @Parameter(name = "increment_size", value = "3"),
          @Parameter(name = "optimizer", value = "hilo")
      })
  @Column(name = "POST_COMMENT_ID", updatable = false, nullable = false)
  private Long id;

  @Column(name = "COMMENT", nullable = false)
  private String comment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "POST_ID", nullable = false)
  private Post post;

  public PostComment() {
    super();
  }

  public PostComment(String comment) {
    this.comment = comment;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
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
        ", comment='" + comment + '\'' +
        '}';
  }
}
