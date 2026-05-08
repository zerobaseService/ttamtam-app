package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalAttachmentRepository extends JpaRepository<JournalAttachment, Long> {
}
