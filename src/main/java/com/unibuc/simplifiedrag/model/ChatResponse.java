package com.unibuc.simplifiedrag.model;

import java.util.List;

public record ChatResponse(String answer,
                           List<String> sourceChunks) {
}
