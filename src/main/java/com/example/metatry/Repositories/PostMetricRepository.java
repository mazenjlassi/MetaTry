package com.example.metatry.Repositories;

import com.example.metatry.Models.PostMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMetricRepository extends JpaRepository<PostMetric, Long> {

    List<PostMetric> findByPostId(Long postId);

}