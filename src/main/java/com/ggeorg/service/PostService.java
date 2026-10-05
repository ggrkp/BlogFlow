package com.ggeorg.service;

import com.ggeorg.domain.Author;
import com.ggeorg.domain.Post;
import com.ggeorg.domain.PostStatus;
import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.post.CreatePostDTO;
import com.ggeorg.dto.request.post.UpdatePostDTO;
import com.ggeorg.dto.response.AuthorSummary;
import com.ggeorg.dto.response.PostResponse;
import com.ggeorg.dto.response.TagSummary;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.AuthorRepository;
import com.ggeorg.repository.PostRepository;
import com.ggeorg.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final AuthorRepository authorRepository;

    private static final Long DEFAULT_AUTHOR_ID = 1L;

    @Autowired
    public PostService(PostRepository postRepository, TagRepository tagRepository, AuthorRepository authorRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.authorRepository = authorRepository;
    }

    @Cacheable(value = "posts", key = "#id")
    @Transactional(readOnly = true)
    public PostResponse getPostResponseById(Long id) {
        Post post = postRepository.findPostWithTagsAndAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));
        return toPostResponseDTO(post);
    }

    @Cacheable(value = "posts", key = "'all'")
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPostResponses() {
        List<Post> posts = postRepository.findAllWithTagsAndAuthor();
        return posts.stream().map(this::toPostResponseDTO).toList();
    }

    @CacheEvict(value = "posts", allEntries = true)
    public Post create(CreatePostDTO request) {
        Author author = authorRepository.findById(DEFAULT_AUTHOR_ID)
                .orElseThrow(() -> new RuntimeException("Default author not found. Please create an author first!"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .status(PostStatus.DRAFT)
                .build();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> relatedTags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(relatedTags);
        }

        return postRepository.save(post);
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post updatePost(Long id, UpdatePostDTO updateRequest) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post to be updated not found"));
        String title = updateRequest.getTitle();
        String content = updateRequest.getContent();
        Set<Long> tagIds = updateRequest.getTagIds();
        if (title != null) {
            post.setTitle(title);
        }
        if (content != null) {
            post.setContent(content);
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> newTags = tagRepository.findAllById(tagIds);
            post.setTags(new HashSet<>(newTags));
        }
        return postRepository.save(post);
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public Post deleteById(Long id) {
        Post deletedPost = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post for deletion not found"));
        postRepository.deleteById(deletedPost.getId());
        return deletedPost;
    }

    private PostResponse toPostResponseDTO(Post post) {
        Author author = post.getAuthor();
        AuthorSummary authorSummary = AuthorSummary.builder()
                .id(author.getId())
                .username(author.getUsername())
                .email(author.getEmail())
                .build();

        Set<TagSummary> tagSummaries = post.getTags().stream()
                .map(tag -> TagSummary.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .build())
                .collect(Collectors.toSet());

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .viewCount(0)
                .author(authorSummary)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .tags(tagSummaries)
                .build();
    }
}
