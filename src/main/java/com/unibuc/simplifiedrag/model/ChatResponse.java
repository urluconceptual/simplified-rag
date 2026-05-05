package com.unibuc.simplifiedrag.model;

import com.unibuc.simplifiedrag.entity.Chunk;

import java.util.List;

public record ChatResponse(String answer,
                           List<Chunk> sourceChunks) {
}
