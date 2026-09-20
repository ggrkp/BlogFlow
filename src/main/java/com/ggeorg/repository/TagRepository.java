package com.ggeorg.repository;

import com.ggeorg.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findAllByOrderByNameAsc();

    List<Tag> findAllByNameContainingIgnoreCase(String name);
}
