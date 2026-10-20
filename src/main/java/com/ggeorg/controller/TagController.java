package com.ggeorg.controller;

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
        return tagService.getAll();
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagDTO request) {
        TagResponse response = tagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        tagService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(@PathVariable("id") Long id, @Valid @RequestBody UpdateTagDTO request) {
        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<TagResponse> getByName(@PathVariable("name") String name) {
            TagResponse response = tagService.getByName(name);
            return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> search(@RequestParam("name") String nameQuery) {
        List<TagResponse> tags = tagService.searchByName(nameQuery);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TagResponse>> createMultipleTags(@Valid @RequestBody List<CreateTagDTO> requests) {
        List<TagResponse> response = tagService.createBulk(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
