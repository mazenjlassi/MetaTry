package com.example.metatry.Repositories;

import com.example.metatry.Models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}