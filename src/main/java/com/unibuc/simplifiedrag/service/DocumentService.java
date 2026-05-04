package com.unibuc.simplifiedrag.service;

import com.unibuc.simplifiedrag.entity.Chunk;
import com.unibuc.simplifiedrag.entity.Document;
import com.unibuc.simplifiedrag.repository.ChunkRepository;
import com.unibuc.simplifiedrag.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final JdbcTemplate jdbc;
    private final TextExtractionService textExtractionService;
    private final ChunkingService chunkingService;

    public List<Document> findAll() {
        return documentRepository.findAllOrderByUploadedAtDesc();
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
    }

    public void storeFile(MultipartFile file) throws Exception {
        validateFile(file);
        String text = extractText(file);
        System.out.println("Extracted: " + text.substring(0, Math.min(200, text.length())));
        Document document = saveDocument(file, text);
        chunkAndEmbed(document, text);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new IllegalArgumentException("Invalid file.");
        }
        String ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase();
        if (!Set.of("PDF", "DOCX", "TXT").contains(ext)) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Please upload PDF, DOCX, or TXT.");
        }
    }

    private Document saveDocument(MultipartFile file, String text) {
        String name = file.getOriginalFilename();
        String ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase();
        Document doc = new Document();
        doc.setFileName(name);
        doc.setFileType(ext);
        doc.setContent(text);
        return documentRepository.save(doc);
    }

    private String extractText(MultipartFile file) throws Exception {
        return textExtractionService.extract(file);
    }

    private void chunkAndEmbed(Document document, String text) {
        List<String> chunks = chunkingService.chunk(text);

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);

            Chunk chunk = new Chunk();
            chunk.setDocumentId(document.getId());
            chunk.setChunkIndex(i);
            chunk.setChunkText(chunkText);
            Chunk saved = chunkRepository.save(chunk);

            embedChunk(saved.getId(), chunkText);
        }
    }

    private void embedChunk(Long chunkId, String chunkText) {
        jdbc.update("""
                    UPDATE chunks
                    SET embedding = VECTOR_EMBEDDING(ALL_MINILM_L12_V2 USING ? AS data)
                    WHERE id = ?
                """, chunkText, chunkId);
    }
}