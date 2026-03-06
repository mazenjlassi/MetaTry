package com.example.metatry.Repositories;

import com.example.metatry.Models.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByActiveTrue();

}