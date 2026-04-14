package com.unibuc.simplifiedrag.controller;

import com.unibuc.simplifiedrag.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @GetMapping
    public String listDocuments(Model model) {
        model.addAttribute("documents", documentService.findAll());
        return "documents/list";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         RedirectAttributes redirectAttributes) {
        try {
            documentService.storeFile(file);
            redirectAttributes.addFlashAttribute("success",
                    "Uploaded: " + file.getOriginalFilename());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Upload failed: " + e.getMessage());
        }
        return "redirect:/documents";
    }
}