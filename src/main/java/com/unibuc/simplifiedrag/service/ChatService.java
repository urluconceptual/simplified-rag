package com.unibuc.simplifiedrag.service;

import com.unibuc.simplifiedrag.entity.Chunk;
import com.unibuc.simplifiedrag.model.ChatResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ChatClient chatClient;

    public ChatResponse ask(List<Long> documentIds, String question) {
        List<Chunk> chunks = retrieveRelevantChunks(documentIds, question);

        chunks.forEach(c -> log.info("Chunk: {}", c.getChunkText().substring(0, Math.min(100, c.getChunkText().length()))));

        String answer = generateAnswer(question, chunks);

        return new ChatResponse(answer, chunks);
    }

    private List<Chunk> retrieveRelevantChunks(List<Long> documentIds, String question) {
    String sql = """
        SELECT *
        FROM chunks
        WHERE document_id IN (:ids)
        ORDER BY VECTOR_DISTANCE(
            embedding,
            VECTOR_EMBEDDING(ALL_MINILM_L12_V2 USING :question AS data),
            COSINE
        )
        FETCH FIRST 5 ROWS ONLY
    """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", documentIds);
        params.addValue("question", question);

        List<Chunk> chunks = namedJdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> {
                    Chunk c = new Chunk();
                    c.setId(rs.getLong("id"));
                    c.setChunkText(rs.getString("chunk_text"));
                    c.setDocumentId(rs.getLong("document_id"));
                    return c;
                }
        );

        log.info("Retrieved {} chunks for question: '{}'",
                chunks.size(), question);

        return chunks;
    }

    private String generateAnswer(String question, List<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return "I could not find any relevant information in the document to answer your question.";
        }

        String context = IntStream.range(0, chunks.size())
                .mapToObj(i -> "Chunk %d:\n%s".formatted(i + 1, chunks.get(i).getChunkText()))
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer = chatClient.prompt()
                .user(u -> u.text("""
            Answer the question based strictly on the context below.
            If the answer is not in the context, say "I don't have enough
            information in this document to answer that question."

            Context:
            {context}

            Question:
            {question}
        """)
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();

        log.info("Generated answer for question: '{}'", question);
        return answer;
    }
}