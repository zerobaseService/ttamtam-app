package com.example.zero.healthcare.journal.repository;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.PostConditionDto;
import com.example.zero.healthcare.repository.JournalPostConditionRepository;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JournalPostConditionRepositoryTest {

    @Autowired private JournalPostConditionRepository postConditionRepository;
    @Autowired private JournalRepository journalRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TestEntityManager em;

    private User saveUser() {
        return userRepository.save(new User("post-" + UUID.randomUUID() + "@test.com", "token", "tester"));
    }

    private WorkoutJournal saveJournal(Long authorId) {
        return journalRepository.save(WorkoutJournal.builder()
                .authorId(authorId)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build());
    }

    private PostConditionDto validDto() {
        PostConditionDto dto = new PostConditionDto();
        dto.setJointMusclePain(6);
        dto.setIntensityFit(7);
        dto.setGoalAchieved(8);
        dto.setDizziness(2);
        dto.setMood(9);
        return dto;
    }

    @Test
    @DisplayName("existsByJournalId는 post_condition이 있으면 true를 반환한다")
    void existsByJournalId_withPostCondition_returnsTrue() {
        User user = saveUser();
        WorkoutJournal journal = saveJournal(user.getId());
        em.flush();

        JournalPostCondition post = JournalPostCondition.of(journal, validDto());
        postConditionRepository.save(post);
        em.flush();
        em.clear();

        assertThat(postConditionRepository.existsByJournalId(journal.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByJournalId는 post_condition이 없으면 false를 반환한다")
    void existsByJournalId_withoutPostCondition_returnsFalse() {
        User user = saveUser();
        WorkoutJournal journal = saveJournal(user.getId());
        em.flush();
        em.clear();

        assertThat(postConditionRepository.existsByJournalId(journal.getId())).isFalse();
    }
}
