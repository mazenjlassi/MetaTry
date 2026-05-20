package com.example.metatry.Repositories;

import com.example.metatry.Models.ContentPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentPatternRepository extends JpaRepository<ContentPattern, Long> {

    Optional<ContentPattern> findByTopicAndPlatform(String topic, String platform);

    Optional<ContentPattern> findByTopic(String topic);

    boolean existsByTopicAndPlatform(String topic, String platform);
}