package com.ggeorg.controller;

import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.dto.request.tag.UpdateTagDTO;
import com.ggeorg.dto.response.TagResponseDTO;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagResponseDTO> getAllTags() {
        List<Tag> tags = tagService.getAll();
        return tags.stream().map(this::toResponseDTO).toList();
    }

    @PostMapping
    public ResponseEntity<TagResponseDTO> createTag(@Valid @RequestBody CreateTagDTO request) {
        Tag tag = tagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagResponseDTO> delete(@PathVariable("id") Long id) {
            tagService.deleteById(id);
            return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDTO> updateTag(@PathVariable("id") Long id, @Valid @RequestBody UpdateTagDTO request) {
            Tag updatedTag = tagService.updateTag(id, request);
            return ResponseEntity.ok(toResponseDTO(updatedTag));
    }

    @GetMapping("/{name}")
    public ResponseEntity<TagResponseDTO> getByName(@PathVariable("name") String name) {
            Tag tag = tagService.getByName(name);
            return ResponseEntity.ok(toResponseDTO(tag));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagResponseDTO>> search(@RequestParam("name") String nameQuery) {
        List<Tag> tags = tagService.searchByName(nameQuery);
        return ResponseEntity.ok(tags.stream().map(this::toResponseDTO).toList());
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TagResponseDTO>> createMultipleTags(@Valid @RequestBody List<CreateTagDTO> requests) {
        List<Tag> tags = tagService.createBulk(requests);
        List<TagResponseDTO> response = tags.stream().map(this::toResponseDTO).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public TagResponseDTO toResponseDTO(Tag tag) {
        return TagResponseDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .postCount(0)
                .build();
    }


}
