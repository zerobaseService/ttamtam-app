package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalPostConditionRepository extends JpaRepository<JournalPostCondition, Long> {
    boolean existsByJournalId(Long journalId);
}
