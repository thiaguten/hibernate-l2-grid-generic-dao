package br.com.thiaguten.model;

import br.com.thiaguten.core.AbstractEntity;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "POST")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Post extends AbstractEntity<Long> {

  private static final long serialVersionUID = 8868996711999017043L;

  @Id
//  @GeneratedValue
  @Column(name = "POST_ID", updatable = false, nullable = false)
  private Long id;

  @Column(name = "TITLE", nullable = false, length = 200)
  private String title;

  // OneToOne shared primary key
  @PrimaryKeyJoinColumn
  @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private PostDetails postDetails;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private List<PostComment> postComments = new ArrayList<>();

  public Post() {
    super();
  }

  public Post(String title, PostDetails postDetails) {
    this.title = title;
    setPostDetails(postDetails);
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public PostDetails getPostDetails() {
    return postDetails;
  }

  public void setPostDetails(PostDetails postDetails) {
    // used to synchronize both sides of the bidirectional association
    if (null == postDetails) {
      if (this.postDetails != null) {
        this.postDetails.setPost(null);
      }
    } else {
      postDetails.setPost(this);
    }
    this.postDetails = postDetails;
  }

  public List<PostComment> getPostComments() {
    return postComments;
  }

//  public void setPostComments(List<PostComment> postComments) {
//    postComments.forEach(this::addPostComment);
//    this.postComments = postComments;
//  }

  public void addPostComment(PostComment postComment) {
    // used to synchronize both sides of the bidirectional association
    this.postComments.add(postComment);
    postComment.setPost(this);
  }

  public void removePostComment(PostComment postComment) {
    // used to synchronize both sides of the bidirectional association
    this.postComments.remove(postComment);
    postComment.setPost(null);
  }

  @Override
  public String toString() {
    return "Post{" +
        "id=" + id +
        ", title='" + title + '\'' +
        '}';
  }

}
