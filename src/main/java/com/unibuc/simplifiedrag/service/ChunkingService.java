package com.unibuc.simplifiedrag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ChunkingService {
    private static final Logger log = LoggerFactory.getLogger(ChunkingService.class);

    private static final int CHUNK_SIZE = 128;
    private static final int CHUNK_OVERLAP = 32;

    public List<String> chunk(String text) {
        text = text.replaceAll("\\s+", " ").strip();
        String[] words = text.split(" ");
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < words.length) {
            int end = Math.min(start + CHUNK_SIZE, words.length);
            while (end < words.length) {
                if (words[end - 1].endsWith(".") || words[end - 1].endsWith("!") || words[end - 1].endsWith("?") || words[end - 1].endsWith(";")) {
                    break;
                }
                end++;
            }

            String chunk = String.join(" ", Arrays.copyOfRange(words, start, end));
            chunks.add(chunk);

            if (end == words.length) break;
            start = end - CHUNK_OVERLAP;
            while (start > 0 && !words[start - 1].endsWith(".") && !words[start - 1].endsWith("!") && !words[start - 1].endsWith("?") && !words[start - 1].endsWith(";")) {
                start--;
            }
            if (start < 0) start = 0;
        }

        log.info("Split text into {} chunks (size={}, overlap={})",
                chunks.size(), CHUNK_SIZE, CHUNK_OVERLAP);
        return chunks;
    }
}