package com.ggeorg.service;

import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.dto.request.tag.UpdateTagDTO;
import com.ggeorg.dto.response.TagResponse;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag spring;

    @BeforeEach
    void setUp() {
        spring = new Tag(1L, "Spring", "Spring Framework", null);
    }

    @Test
    void create_savesTagAndReturnsResponse() {
        CreateTagDTO request = new CreateTagDTO("Java", "Java Programming");
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TagResponse result = tagService.create(request);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(captor.capture());
        assertEquals("Java", captor.getValue().getName());
        assertEquals("Java Programming", captor.getValue().getDescription());
        assertEquals(2L, result.getId());
        assertEquals("Java", result.getName());
        assertEquals(0, result.getPostCount());
    }

    @Test
    void getByName_returnsTagWhenFound() {
        when(tagRepository.findByName("Spring")).thenReturn(Optional.of(spring));

        TagResponse result = tagService.getByName("Spring");

        assertEquals(1L, result.getId());
        assertEquals("Spring", result.getName());
    }

    @Test
    void getByName_throwsWhenMissing() {
        when(tagRepository.findByName("Missing")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tagService.getByName("Missing"));
        assertTrue(ex.getMessage().contains("Missing"));
    }

    @Test
    void getAll_returnsTagsOrderedByName() {
        when(tagRepository.findAllByOrderByNameAsc()).thenReturn(List.of(spring));

        List<TagResponse> result = tagService.getAll();

        assertEquals(1, result.size());
        assertEquals("Spring", result.get(0).getName());
    }

    @Test
    void searchByName_usesCaseInsensitiveQuery() {
        when(tagRepository.findAllByNameContainingIgnoreCase("spr")).thenReturn(List.of(spring));

        List<TagResponse> result = tagService.searchByName("spr");

        assertEquals(1, result.size());
        verify(tagRepository).findAllByNameContainingIgnoreCase("spr");
    }

    @Test
    void updateTag_changesOnlyProvidedFields() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(spring));
        when(tagRepository.save(spring)).thenReturn(spring);

        TagResponse result = tagService.updateTag(1L, UpdateTagDTO.builder().description("Updated").build());

        assertEquals("Spring", result.getName());
        assertEquals("Updated", result.getDescription());
    }

    @Test
    void updateTag_throwsWhenMissing() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tagService.updateTag(99L, UpdateTagDTO.builder().name("X").build()));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void deleteById_deletesExistingTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(spring));

        tagService.deleteById(1L);

        verify(tagRepository).deleteById(1L);
    }

    @Test
    void deleteById_throwsWhenMissing() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.deleteById(99L));
        verify(tagRepository, never()).deleteById(any());
    }

    @Test
    void createBulk_savesEveryRequest() {
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<CreateTagDTO> requests = List.of(
                new CreateTagDTO("Java", "Java Programming"),
                new CreateTagDTO("Redis", "In-memory store"));

        List<TagResponse> result = tagService.createBulk(requests);

        assertEquals(2, result.size());
        verify(tagRepository, times(2)).save(any(Tag.class));
    }
}
