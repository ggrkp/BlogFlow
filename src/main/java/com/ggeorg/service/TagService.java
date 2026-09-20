package com.ggeorg.service;

import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.dto.request.tag.UpdateTagDTO;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.TagRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/// / Read operations (readOnly = true)
//getById(Long id)
//getAll(Pageable pageable)
//findByName(String name)
//

/// / Write operations (transactional)
//create(CreateDTO request)
//update(Long id, UpdateDTO request)
//delete(Long id)

@Service
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag create(CreateTagDTO request) {
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        return tagRepository.save(tag);
    }

    public Tag getByName(String name) {
        Optional<Tag> tag = tagRepository.findByName(name);
        return tag.orElseThrow(() -> new ResourceNotFoundException("Tag with name " + name + " not found."));
    }

    public Tag deleteById(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tagRepository.deleteById(id);
        return tag;
    }

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public List<Tag> searchByName(String nameQuery) {
        return tagRepository.findAllByNameContainingIgnoreCase(nameQuery);
    }

    @Transactional
    public Tag updateTag(Long id, UpdateTagDTO request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        if (request.getName() != null) {
            tag.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }
        return tagRepository.save(tag);
    }

    @Transactional
    public List<Tag> createBulk(@Valid List<CreateTagDTO> requests) {
        return requests.stream()
                .map(this::create)
                .toList();
    }
}
