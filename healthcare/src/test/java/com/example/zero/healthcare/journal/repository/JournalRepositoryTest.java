package com.example.zero.healthcare.journal.repository;

import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JournalRepositoryTest {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManager entityManager;

    private User saveUser() {
        User user = new User("test-" + UUID.randomUUID() + "@example.com", "token", "tester");
        return userRepository.save(user);
    }

    private WorkoutJournal buildJournal(Long authorId) {
        return buildJournal(authorId, LocalDate.of(2026, 4, 20));
    }

    private WorkoutJournal buildJournal(Long authorId, LocalDate workoutDate) {
        return WorkoutJournal.builder()
                .authorId(authorId)
                .workoutDate(workoutDate)
                .preJointMusclePain(5)
                .preSleepHours(7)
                .preSleepQuality(6)
                .prePreviousFatigue(4)
                .preOverallCondition(8)
                .build();
    }

    @Test
    @DisplayName("일지 저장 시 painRecords가 cascade PERSIST로 함께 저장된다")
    void save_withPainRecords_persistsCascade() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());

        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.PRE).bodyPart(BodyPart.SHOULDER).side(BodySide.LEFT).painLevel(7).build());
        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.PRE).bodyPart(BodyPart.KNEE).side(BodySide.RIGHT).painLevel(4).build());

        WorkoutJournal saved = journalRepository.save(journal);
        em.flush();
        em.clear();

        WorkoutJournal found = journalRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPainRecords()).hasSize(2);
    }

    @Test
    @DisplayName("findByAuthorId로 해당 유저의 일지를 조회한다")
    void findByAuthorId_returnsJournalsOfAuthor() {
        User user = saveUser();
        journalRepository.save(buildJournal(user.getId()));
        journalRepository.save(buildJournal(user.getId()));
        em.flush();
        em.clear();

        List<WorkoutJournal> journals = journalRepository.findByAuthorId(user.getId());

        assertThat(journals).hasSize(2);
    }

    @Test
    @DisplayName("delete() 호출 시 workout_journal은 물리 삭제되지 않고 deleted_at이 세팅된다")
    void delete_softDeletesJournal() {
        User user = saveUser();
        WorkoutJournal saved = journalRepository.save(buildJournal(user.getId()));
        em.flush();

        journalRepository.delete(saved);
        em.flush();
        em.clear();

        // @SQLRestriction 우회: 네이티브 쿼리로 직접 조회
        Long count = (Long) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM workout_journal WHERE id = :id AND deleted_at IS NOT NULL")
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("delete() 이후 journal_pain_record 행은 물리 삭제되지 않고 남는다")
    void delete_painRecordsNotPhysicallyDeleted() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());
        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.PRE).bodyPart(BodyPart.KNEE).side(BodySide.LEFT).painLevel(5).build());

        WorkoutJournal saved = journalRepository.save(journal);
        em.flush();
        em.clear(); // session 초기화 — pain records가 session에 없는 상태에서 delete

        // LAZY 로딩: findById는 journal만 session에 로드(pain records는 비로드)
        WorkoutJournal toDelete = journalRepository.findById(saved.getId()).orElseThrow();
        journalRepository.delete(toDelete);
        em.flush(); // journal만 session에 있으므로 TransientPropertyValueException 없음
        em.clear();

        Long count = (Long) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM journal_pain_record WHERE journal_id = :jid")
                .setParameter("jid", saved.getId())
                .getSingleResult();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("soft delete된 일지는 findByAuthorId 결과에서 자동 제외된다")
    void findByAuthorId_excludesSoftDeletedJournals() {
        User user = saveUser();
        WorkoutJournal active = journalRepository.save(buildJournal(user.getId()));
        WorkoutJournal toDelete = journalRepository.save(buildJournal(user.getId()));
        em.flush();

        journalRepository.delete(toDelete);
        em.flush();
        em.clear();

        List<WorkoutJournal> journals = journalRepository.findByAuthorId(user.getId());

        assertThat(journals).hasSize(1);
        assertThat(journals.get(0).getId()).isEqualTo(active.getId());
    }

    @Test
    @DisplayName("특정일로 조회하면 해당 날짜 일지만 반환한다")
    void findByAuthorIdAndWorkoutDate_returnsOnlySameDay() {
        User user = saveUser();
        LocalDate targetDate = LocalDate.of(2026, 4, 20);
        LocalDate otherDate = LocalDate.of(2026, 4, 21);

        journalRepository.save(buildJournal(user.getId(), targetDate));
        journalRepository.save(buildJournal(user.getId(), otherDate));
        em.flush();
        em.clear();

        List<WorkoutJournal> result = journalRepository
                .findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(user.getId(), targetDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWorkoutDate()).isEqualTo(targetDate);
    }

    @Test
    @DisplayName("기간 조회는 from/to 날짜를 포함하여 반환한다")
    void findByAuthorIdAndWorkoutDateBetween_returnsInclusiveRange() {
        User user = saveUser();

        journalRepository.save(buildJournal(user.getId(), LocalDate.of(2026, 3, 31)));
        journalRepository.save(buildJournal(user.getId(), LocalDate.of(2026, 4, 1)));
        journalRepository.save(buildJournal(user.getId(), LocalDate.of(2026, 4, 15)));
        journalRepository.save(buildJournal(user.getId(), LocalDate.of(2026, 4, 30)));
        journalRepository.save(buildJournal(user.getId(), LocalDate.of(2026, 5, 1)));
        em.flush();
        em.clear();

        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 30);
        List<WorkoutJournal> result = journalRepository
                .findByAuthorIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(user.getId(), from, to);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(j ->
                !j.getWorkoutDate().isBefore(from) && !j.getWorkoutDate().isAfter(to));
    }

    @Test
    @DisplayName("일지는 workoutDate DESC, createdAt DESC로 정렬된다")
    void findByAuthorId_sortedByWorkoutDateDescThenCreatedAtDesc() {
        User user = saveUser();
        LocalDate earlier = LocalDate.of(2026, 4, 1);
        LocalDate later = LocalDate.of(2026, 4, 20);

        journalRepository.save(buildJournal(user.getId(), earlier));
        journalRepository.save(buildJournal(user.getId(), later));
        em.flush();
        em.clear();

        List<WorkoutJournal> result = journalRepository
                .findByAuthorIdOrderByWorkoutDateDescCreatedAtDesc(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getWorkoutDate()).isEqualTo(later);
        assertThat(result.get(1).getWorkoutDate()).isEqualTo(earlier);
    }

    @Test
    @DisplayName("pre 컨디션 없이 일지를 저장할 수 있다 (pre-* nullable)")
    void save_withoutPreCondition_succeeds() {
        User user = saveUser();
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(user.getId())
                .workoutDate(LocalDate.of(2026, 4, 20))
                .build();

        WorkoutJournal saved = journalRepository.save(journal);
        em.flush();
        em.clear();

        WorkoutJournal found = journalRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPreJointMusclePain()).isNull();
        assertThat(found.getPreSleepHours()).isNull();
    }

    @Test
    @DisplayName("같은 날 여러 일지를 저장해도 모두 저장된다 (UNIQUE 없음)")
    void sameAuthorMultipleOnSameDate_allPersisted() {
        User user = saveUser();
        LocalDate date = LocalDate.of(2026, 4, 20);

        journalRepository.save(buildJournal(user.getId(), date));
        journalRepository.save(buildJournal(user.getId(), date));
        em.flush();
        em.clear();

        List<WorkoutJournal> result = journalRepository
                .findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(user.getId(), date);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("attachments가 cascade PERSIST로 함께 저장되고 displayOrder 오름차순으로 조회된다")
    void save_withAttachments_cascadePersistsAndSortedByDisplayOrder() {
        User user = saveUser();
        WorkoutJournal journal = buildJournal(user.getId());

        journal.addAttachment(JournalAttachment.builder()
                .imageUrl("https://cdn.ttamtam.app/img1.jpg").displayOrder(1).build());
        journal.addAttachment(JournalAttachment.builder()
                .imageUrl("https://cdn.ttamtam.app/img0.jpg").displayOrder(0).build());

        WorkoutJournal saved = journalRepository.save(journal);
        em.flush();
        em.clear();

        WorkoutJournal found = journalRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getAttachments()).hasSize(2);
        assertThat(found.getAttachments().get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(found.getAttachments().get(1).getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNull은 PRE-only 일지 중 가장 최근 것을 반환한다")
    void findFirstPreOnly_returnsLatestMatchingJournal() {
        User user = saveUser();
        LocalDate date = LocalDate.of(2026, 4, 20);

        WorkoutJournal first = journalRepository.save(buildJournal(user.getId(), date));
        em.flush();
        WorkoutJournal second = journalRepository.save(buildJournal(user.getId(), date));
        em.flush();
        em.clear();

        java.util.Optional<WorkoutJournal> result = journalRepository
                .findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
                        user.getId(), date);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("postRecordedAt이 있는 일지는 findFirstPreOnly 조회에서 제외된다")
    void findFirstPreOnly_excludesJournalsWithPostRecordedAt() {
        User user = saveUser();
        LocalDate date = LocalDate.of(2026, 4, 20);

        WorkoutJournal completed = buildJournal(user.getId(), date);
        completed.applyPostCondition(6, 7, 8, 2, 9, "완료");
        journalRepository.save(completed);
        em.flush();
        em.clear();

        java.util.Optional<WorkoutJournal> result = journalRepository
                .findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
                        user.getId(), date);

        assertThat(result).isEmpty();
    }
}
