package com.example.metatry.Repositories;

import com.example.metatry.Models.ScrapedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapedPostRepository extends JpaRepository<ScrapedPost, Long> {

    List<ScrapedPost> findByCompanyName(String companyName);

    List<ScrapedPost> findByPlatform(String platform);

    List<ScrapedPost> findByTopic(String topic);

    List<ScrapedPost> findByCompanyNameAndPlatform(String companyName, String platform);

    List<ScrapedPost> findByUsedForPatternFalse();

    long countByUsedForPatternFalse();

    List<ScrapedPost> findTop30ByUsedForPatternFalse();

    long countByCompanyNameAndUsedForPatternFalse(String companyName);

    List<ScrapedPost> findTop30ByCompanyNameAndUsedForPatternFalse(String companyName);

    long countByCompanyName(String companyName);

    long countByPlatform(String platform);
}