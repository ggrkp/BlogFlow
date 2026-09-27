package com.ggeorg.controller;

import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.dto.request.tag.UpdateTagDTO;
import com.ggeorg.dto.response.TagResponse;
import com.ggeorg.service.TagService;
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
    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagService.getAll();
        return tags.stream().map(this::toResponseDTO).toList();
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagDTO request) {
        Tag tag = tagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagResponse> delete(@PathVariable("id") Long id) {
            tagService.deleteById(id);
            return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(@PathVariable("id") Long id, @Valid @RequestBody UpdateTagDTO request) {
            Tag updatedTag = tagService.updateTag(id, request);
            return ResponseEntity.ok(toResponseDTO(updatedTag));
    }

    @GetMapping("/{name}")
    public ResponseEntity<TagResponse> getByName(@PathVariable("name") String name) {
            Tag tag = tagService.getByName(name);
            return ResponseEntity.ok(toResponseDTO(tag));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> search(@RequestParam("name") String nameQuery) {
        List<Tag> tags = tagService.searchByName(nameQuery);
        return ResponseEntity.ok(tags.stream().map(this::toResponseDTO).toList());
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TagResponse>> createMultipleTags(@Valid @RequestBody List<CreateTagDTO> requests) {
        List<Tag> tags = tagService.createBulk(requests);
        List<TagResponse> response = tags.stream().map(this::toResponseDTO).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public TagResponse toResponseDTO(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .postCount(0)
                .build();
    }


}
