package com.unibuc.simplifiedrag.controller;

import com.unibuc.simplifiedrag.model.ChatResponse;
import com.unibuc.simplifiedrag.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chat")
@AllArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{documentId}")
    public String chatPage(@PathVariable Long documentId, Model model) {
        model.addAttribute("documentId", documentId);
        model.addAttribute("answer", null);
        model.addAttribute("question", null);
        return "chat/chat";
    }

    @PostMapping("/{documentId}")
    public String ask(@PathVariable Long documentId,
                      @RequestParam("question") String question,
                      Model model) {
        model.addAttribute("documentId", documentId);
        model.addAttribute("question", question);
        try {
            ChatResponse response = chatService.ask(documentId, question);
            model.addAttribute("answer", response.answer());
            model.addAttribute("sources", response.sourceChunks());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to answer: " + e.getMessage());
        }
        return "chat/chat";
    }
}