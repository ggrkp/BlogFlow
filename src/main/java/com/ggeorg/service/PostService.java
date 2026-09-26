package com.ggeorg.service;

import com.ggeorg.domain.Author;
import com.ggeorg.domain.Post;
import com.ggeorg.domain.PostStatus;
import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.post.CreatePostDTO;
import com.ggeorg.dto.request.post.UpdatePostDTO;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.AuthorRepository;
import com.ggeorg.repository.PostRepository;
import com.ggeorg.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Post getPostById(Long id) {
        return postRepository.findPostWithTagsAndAuthor(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));
    }

    public Post create(CreatePostDTO request) {
        Author author = authorRepository.findById(DEFAULT_AUTHOR_ID)
                .orElseThrow(() -> new RuntimeException("Default author not found. Please create an author first."));

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

    public List<Post> getAllPosts() {
        return postRepository.findAllWithTagsAndAuthor();
    }

    @Transactional
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
    public Post deleteById(Long id) {
        Post deletedPost = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post for deletion not found"));
        postRepository.deleteById(deletedPost.getId());
        return deletedPost;
    }
}
