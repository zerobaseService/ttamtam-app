package com.example.zero.healthcare.journal.repository;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import com.example.zero.healthcare.repository.JournalPreConditionRepository;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JournalPreConditionRepositoryTest {

    @Autowired private JournalPreConditionRepository preConditionRepository;
    @Autowired private JournalRepository journalRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TestEntityManager em;

    private User saveUser() {
        return userRepository.save(new User("pre-" + UUID.randomUUID() + "@test.com", "token", "tester"));
    }

    private WorkoutJournal buildJournal(Long authorId) {
        return WorkoutJournal.builder()
                .authorId(authorId)
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build();
    }

    private PreConditionDto validDto() {
        return new PreConditionDto(5, 7, 6, 4, 8);
    }

    @Test
    @DisplayName("journal cascade로 pre_condition도 함께 저장된다")
    void save_cascadeFromJournal_persists() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());
        JournalPreCondition pre = JournalPreCondition.of(journal, validDto());
        journal.setPreCondition(pre);

        journalRepository.save(journal);
        em.flush();
        em.clear();

        Optional<JournalPreCondition> found = preConditionRepository.findByJournalId(journal.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOverallCondition()).isEqualTo(8);
    }

    @Test
    @DisplayName("findByJournalId는 해당 일지의 pre_condition을 반환한다")
    void findByJournalId_returnsPreCondition() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());
        JournalPreCondition pre = JournalPreCondition.of(journal, validDto());
        journal.setPreCondition(pre);

        journalRepository.save(journal);
        em.flush();
        em.clear();

        Optional<JournalPreCondition> found = preConditionRepository.findByJournalId(journal.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSleepHours()).isEqualTo(7);
        assertThat(found.get().getJointMusclePain()).isEqualTo(5);
    }

    @Test
    @DisplayName("pre_condition이 없는 일지 조회 시 empty를 반환한다")
    void findByJournalId_noPreCondition_returnsEmpty() {
        User user = saveUser();
        WorkoutJournal journal = journalRepository.save(buildJournal(user.getId()));
        em.flush();
        em.clear();

        Optional<JournalPreCondition> found = preConditionRepository.findByJournalId(journal.getId());
        assertThat(found).isEmpty();
    }
}
