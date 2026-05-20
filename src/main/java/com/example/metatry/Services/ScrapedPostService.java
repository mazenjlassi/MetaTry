package com.example.metatry.Services;

import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ScrapedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapedPostService {

    private final ScrapedPostRepository scrapedPostRepository;

    public List<ScrapedPost> getAll() {
        return scrapedPostRepository.findAll();
    }

    public ScrapedPost getById(Long id) {
        return scrapedPostRepository.findById(id).orElse(null);
    }

    public List<ScrapedPost> getByCompanyName(String companyName) {
        return scrapedPostRepository.findByCompanyName(companyName);
    }

    public List<ScrapedPost> getByPlatform(String platform) {
        return scrapedPostRepository.findByPlatform(platform);
    }

    public List<ScrapedPost> getByTopic(String topic) {
        return scrapedPostRepository.findByTopic(topic);
    }

    public ScrapedPost save(ScrapedPost post) {
        return scrapedPostRepository.save(post);
    }

    public void delete(Long id) {
        scrapedPostRepository.deleteById(id);
    }

    public long countByCompany(String companyName) {
        return scrapedPostRepository.countByCompanyName(companyName);
    }

    public long countByPlatform(String platform) {
        return scrapedPostRepository.countByPlatform(platform);
    }

    public List<ScrapedPost> getUnusedForPattern() {
        return scrapedPostRepository.findByUsedForPatternFalse();
    }

    public void markAsUsedForPattern(Long id) {
        scrapedPostRepository.findById(id).ifPresent(post -> {
            post.setUsedForPattern(true);
            scrapedPostRepository.save(post);
        });
    }
}