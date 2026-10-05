package com.ggeorg.service;

import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.dto.request.tag.UpdateTagDTO;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.TagRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @CacheEvict(value = "tags", allEntries = true)
    public Tag create(CreateTagDTO request) {
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        return tagRepository.save(tag);
    }

    @Cacheable(value = "tags", key = "#name")
    public Tag getByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with name " + name + " not found."));
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public Tag deleteById(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tagRepository.deleteById(id);
        return tag;
    }

    @Cacheable(value = "tags", key = "'all'")
    public List<Tag> getAll() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    @Cacheable(value = "tags", key = "#nameQuery")
    public List<Tag> searchByName(String nameQuery) {
        return tagRepository.findAllByNameContainingIgnoreCase(nameQuery);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public Tag updateTag(Long id, UpdateTagDTO request) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        if (request.getName() != null) {
            tag.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }
        return tagRepository.save(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public List<Tag> createBulk(@Valid List<CreateTagDTO> requests) {
        return requests.stream().map(this::create).toList();
    }
}
