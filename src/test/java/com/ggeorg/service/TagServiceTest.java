package com.ggeorg.service;

import com.ggeorg.domain.Tag;
import com.ggeorg.dto.request.tag.CreateTagDTO;
import com.ggeorg.exception.ResourceNotFoundException;
import com.ggeorg.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private Tag testTag;
    private CreateTagDTO createTagDTO;

    @BeforeEach
    void setUp() {
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Spring");
        testTag.setDescription("Spring Framework");

        createTagDTO = new CreateTagDTO();
        createTagDTO.setName("Java");
        createTagDTO.setDescription("Java Programming");
    }

    @Test
    void testCreateTag() {
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

        Tag result = tagService.create(createTagDTO);

        assertNotNull(result);
        assertEquals("Spring", result.getName());
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void testGetByName_Success() {
        when(tagRepository.findByName("Spring")).thenReturn(Optional.of(testTag));

        Tag result = tagService.getByName("Spring");

        assertNotNull(result);
        assertEquals("Spring", result.getName());
        verify(tagRepository, times(1)).findByName("Spring");
    }

    @Test
    void testGetByName_NotFound() {
        when(tagRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.getByName("NonExistent");
        });

        verify(tagRepository, times(1)).findByName("NonExistent");
    }

    @Test
    void testSearchByName() {
        List<Tag> tags = List.of(testTag);
        when(tagRepository.findAllByNameContainingIgnoreCase("Spring")).thenReturn(tags);

        List<Tag> result = tagService.searchByName("Spring");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Spring", result.get(0).getName());
        verify(tagRepository, times(1)).findAllByNameContainingIgnoreCase("Spring");
    }

    @Test
    void testGetAll() {
        List<Tag> tags = List.of(testTag);
        when(tagRepository.findAll()).thenReturn(tags);

        List<Tag> result = tagService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tagRepository, times(1)).findAll();
    }
}
