package com.ggeorg.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PostView> views = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "post_tag", joinColumns = {
            @JoinColumn(name = "post_id")
    }, inverseJoinColumns = {
            @JoinColumn(name = "tag_id")
    })
    private Set<Tag> tags = new HashSet<>();

    @Version
    private Long version;

    // Helper for comments (bidirectional)
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);  // Maintain both sides
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);  // Maintain both sides
    }

    // Helper for tags (if bidirectional)
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);  // Assuming Tag has Set<Blog> blogs
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getPosts().remove(this);
    }

}
