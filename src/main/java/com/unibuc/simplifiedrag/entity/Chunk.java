package com.unibuc.simplifiedrag.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chunks")
@Getter
@Setter
public class Chunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "chunk_index")
    private int chunkIndex;

    @Column(name = "chunk_text")
    private String chunkText;
}