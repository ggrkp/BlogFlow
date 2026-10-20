package com.ggeorg.service;

import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.dto.request.tag.UpdateTagDTO;
import com.ggeorg.dto.response.TagResponse;
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
    public TagResponse create(CreateTagDTO request) {
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        Tag saved = tagRepository.save(tag);
        return toTagResponseDTO(saved);
    }

    @Cacheable(value = "tags", key = "#name")
    @Transactional(readOnly = true)
    public TagResponse getByName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag with name " + name + " not found."));
        return toTagResponseDTO(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void deleteById(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tagRepository.deleteById(id);
    }

    @Cacheable(value = "tags", key = "'all'")
    @Transactional(readOnly = true)
    public List<TagResponse> getAll() {
        return tagRepository.findAllByOrderByNameAsc().stream()
                .map(this::toTagResponseDTO)
                .toList();
    }

    @Cacheable(value = "tags", key = "#nameQuery")
    @Transactional(readOnly = true)
    public List<TagResponse> searchByName(String nameQuery) {
        return tagRepository.findAllByNameContainingIgnoreCase(nameQuery).stream()
                .map(this::toTagResponseDTO)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagResponse updateTag(Long id, UpdateTagDTO request) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        if (request.getName() != null) {
            tag.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }
        Tag updated = tagRepository.save(tag);
        return toTagResponseDTO(updated);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public List<TagResponse> createBulk(@Valid List<CreateTagDTO> requests) {
        return requests.stream()
                .map(request -> {
                    Tag tag = new Tag();
                    tag.setName(request.getName());
                    tag.setDescription(request.getDescription());
                    Tag saved = tagRepository.save(tag);
                    return toTagResponseDTO(saved);
                })
                .toList();
    }

    private TagResponse toTagResponseDTO(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .postCount(0)
                .build();
    }
}
