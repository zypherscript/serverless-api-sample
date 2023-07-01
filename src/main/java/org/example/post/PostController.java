package org.example.post;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/posts")
public class PostController {

  private static final Logger log = LoggerFactory.getLogger(PostController.class);
  private final JsonPlaceholderService jsonPlaceholderService;

  private List<Post> posts = new ArrayList<>();

  public PostController(JsonPlaceholderService jsonPlaceholderService) {
    this.jsonPlaceholderService = jsonPlaceholderService;
  }

  @GetMapping
  public ResponseEntity<List<Post>> findAll() {
    return ResponseEntity.ok(this.posts);
  }

  @GetMapping("/{requestedId}")
  public ResponseEntity<Post> findById(@PathVariable Long requestedId) {
    return this.posts
        .stream()
        .filter(post -> post.id().equals(requestedId))
        .findFirst()
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  private ResponseEntity<Void> createPost(@RequestBody Post post, UriComponentsBuilder ucb) {
    this.posts.add(post);
    var locationOfNewPost = ucb
        .path("api/posts/{id}")
        .buildAndExpand(post.id())
        .toUri();
    return ResponseEntity.created(locationOfNewPost).build();
  }

  @PutMapping("/{requestedId}")
  private ResponseEntity<Void> putPost(@PathVariable Long requestedId,
      @RequestBody Post updatePost) {
    var postOptional = this.posts.stream()
        .filter(post -> post.id().equals(requestedId))
        .findFirst();
    if (postOptional.isPresent()) {
      this.posts.set(posts.indexOf(postOptional.get()), updatePost);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  private ResponseEntity<Void> deletePost(@PathVariable Long id) {
    var postOptional = this.posts.stream().filter(post -> post.id().equals(id)).findFirst();
    if (postOptional.isPresent()) {
      this.posts.remove(postOptional.get());
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @PostConstruct
  private void init() {
    if (this.posts.isEmpty()) {
      log.info("init posts");
      posts = jsonPlaceholderService.loadPosts();
    }
  }

}
