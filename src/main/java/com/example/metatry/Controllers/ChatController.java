package com.example.metatry.Controllers;

import com.example.metatry.DTOs.*;
import com.example.metatry.Services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // adjust later
public class ChatController {

    private final ChatService chatService;

    // ================= CONVERSATIONS =================

    @GetMapping("/conversations")
    public List<ConversationDTO> getAll() {
        return chatService.getAllConversations();
    }

    @GetMapping("/conversations/{id}")
    public ConversationDTO getById(@PathVariable Long id) {
        return chatService.getConversation(id);
    }

    @PostMapping("/conversations")
    public ConversationDTO create(@RequestBody CreateConversationRequest request) {
        return chatService.createConversation(request.getTitle());
    }

    @DeleteMapping("/conversations/{id}")
    public void delete(@PathVariable Long id) {
        chatService.deleteConversation(id);
    }

    // ================= MESSAGES =================

    @GetMapping("/conversations/{id}/messages")
    public List<MessageDTO> getMessages(@PathVariable Long id) {
        return chatService.getMessages(id);
    }

    @PostMapping("/conversations/{id}/messages")
    public MessageDTO sendMessage(
            @PathVariable Long id,
            @RequestBody CreateMessageRequest request
    ) {
        return chatService.sendMessage(id, request.getContent());
    }

    // ================= CONCLUSION =================

    @PostMapping("/conversations/{id}/conclusion")
    public String generateConclusion(@PathVariable Long id) {
        return chatService.generateConclusion(id);
    }
}