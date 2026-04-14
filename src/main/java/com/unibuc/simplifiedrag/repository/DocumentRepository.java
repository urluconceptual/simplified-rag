package com.unibuc.simplifiedrag.repository;

import com.unibuc.simplifiedrag.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query("SELECT d FROM Document d ORDER BY d.uploadedAt DESC")
    List<Document> findAllOrderByUploadedAtDesc();
}