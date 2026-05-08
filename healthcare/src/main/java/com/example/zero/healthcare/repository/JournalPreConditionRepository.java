package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JournalPreConditionRepository extends JpaRepository<JournalPreCondition, Long> {
    Optional<JournalPreCondition> findByJournalId(Long journalId);
}
