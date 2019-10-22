package br.com.thiaguten.model;

import br.com.thiaguten.converter.LocalDateTimeConverter;
import br.com.thiaguten.core.AbstractEntity;
import java.time.LocalDateTime;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "POST_DETAILS")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PostDetails extends AbstractEntity<Long> {

  @Id
//  @Column(name = "POST_DETAILS_ID", updatable = false, nullable = false)
  private Long id;

  @Column(name = "CREATED_BY", nullable = false)
  private String createdBy;

  @Column(name = "CREATED_ON", nullable = false)
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime createdOn;

  // OneToOne shared primary key
  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "POST_DETAILS_ID", referencedColumnName = "POST_ID", nullable = false)
  private Post post;

  public PostDetails() {
    super();
  }

  public PostDetails(String createdBy) {
    this.createdBy = createdBy;
    this.createdOn = LocalDateTime.now();
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }

  public Post getPost() {
    return post;
  }

  public void setPost(Post post) {
    this.post = post;
  }

  @Override
  public String toString() {
    return "PostDetails{" +
        "id=" + id +
        ", createdBy='" + createdBy + '\'' +
        ", createdOn=" + createdOn +
        '}';
  }
}
