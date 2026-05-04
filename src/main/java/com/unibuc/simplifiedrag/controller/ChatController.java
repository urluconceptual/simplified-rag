package com.unibuc.simplifiedrag.controller;

import com.unibuc.simplifiedrag.entity.Chunk;
import com.unibuc.simplifiedrag.entity.Document;
import com.unibuc.simplifiedrag.entity.Item;
import com.unibuc.simplifiedrag.entity.ItemForm;
import com.unibuc.simplifiedrag.model.ChatResponse;
import com.unibuc.simplifiedrag.service.ChatService;
import com.unibuc.simplifiedrag.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
@AllArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final DocumentService documentService;

    @GetMapping("/{documentId}")
    public String chatPage(@PathVariable Long documentId, Model model) {
        model.addAttribute("documentId", documentId);
        model.addAttribute("answer", null);
        model.addAttribute("question", null);
        return "chat/chat";
    }

    @GetMapping("/documents")
    public String selectDocument(Model model) {
        List<Document> documents = documentService.findAll();
        List<Item> items = List.of(
                documents.stream()
                        .map(d -> new Item(d.getId(), d.getFileName(), false))
                        .toArray(Item[]::new)
        );
        ItemForm form = new ItemForm();
        form.setItems(items);

        model.addAttribute("itemForm", form);
        model.addAttribute("responseGenerated", false);

        return "chat/docs";
    }

    @PostMapping("/documents")
    public String askQuestion(@RequestParam("question") String question, @ModelAttribute ItemForm docs, Model model) {
        model.addAttribute("question", question);
        List<Long> selectedDocIds = docs.getItems().stream()
                .filter(Item::isSelected)
                .map(Item::getId)
                .toList();
        if (selectedDocIds.isEmpty()) {
            model.addAttribute("error", "Please select at least one document.");
        }
        try {
            ChatResponse response = chatService.ask(selectedDocIds, question);
            docs.setItems(docs.getItems().stream().map(item -> {
                item.setName(documentService.findById(item.getId()).getFileName());
                return item;
            }).toList());
            model.addAttribute("itemForm", docs);
            model.addAttribute("responseGenerated", response.answer() != null);
            model.addAttribute("answer", response.answer());
            model.addAttribute("sources", response.sourceChunks());
            List<Item> selectedDocs = docs.getItems().stream()
                    .filter(Item::isSelected)
                    .toList();
            Map<Long, String> sourcesMap = response.sourceChunks().stream()
                    .collect(Collectors.toMap(Chunk::getId, d -> documentService.findById(d.getDocumentId()).getFileName()));
            model.addAttribute("sourcesMap", sourcesMap);
            ItemForm selectedForm = new ItemForm();
            selectedForm.setItems(selectedDocs);
            model.addAttribute("selectedForm", selectedForm);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to answer: " + e.getMessage());
        }
        return "chat/docs";
    }

}