package com.example.zero.healthcare.journal.service;

import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.CompleteJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.JournalDetailDto;
import com.example.zero.healthcare.dto.journal.JournalSummaryDto;
import com.example.zero.healthcare.dto.journal.PainRecordDto;
import com.example.zero.healthcare.dto.journal.PostConditionDto;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalPostRequest;
import com.example.zero.healthcare.exception.journal.JournalNotFoundException;
import com.example.zero.healthcare.exception.journal.PostAlreadyRecordedException;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import com.example.zero.healthcare.service.JournalService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JournalRepository journalRepository;
    @InjectMocks private JournalService journalService;

    private PreConditionDto validPreCondition() {
        return new PreConditionDto(5, 7, 6, 4, 8);
    }

    private LocalDate validDate() {
        return LocalDate.of(2026, 4, 20);
    }

    private WorkoutJournal stubSavedJournal(LocalDate workoutDate) {
        return WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(workoutDate)
                .preJointMusclePain(5).preSleepHours(7)
                .preSleepQuality(6).prePreviousFatigue(4).preOverallCondition(8)
                .build();
    }

    @Test
    @DisplayName("유효한 요청으로 일지를 생성하면 journalId와 createdAt을 반환한다")
    void createJournal_withValidRequest_returnsJournalIdAndPersists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("test@example.com", "token", "tester")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), List.of());
        CreateJournalResponse response = journalService.createJournal(1L, req);

        assertThat(response).isNotNull();
        verify(journalRepository).save(any(WorkoutJournal.class));
    }

    @Test
    @DisplayName("painRecords가 빈 배열이면 pain record 없이 일지만 저장된다")
    void createJournal_withEmptyPainRecords_persistsJournalOnly() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), List.of());
        journalService.createJournal(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getPainRecords()).isEmpty();
    }

    @Test
    @DisplayName("painRecords가 null이어도 일지는 정상 저장되고 pain record는 0건이다")
    void createJournal_withNullPainRecords_persistsJournalOnly() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), null);
        journalService.createJournal(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getPainRecords()).isEmpty();
    }

    @Test
    @DisplayName("저장된 모든 JournalPainRecord의 timing은 PRE이다")
    void createJournal_persistedPainRecord_hasTimingPre() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        List<PainRecordDto> records = List.of(
                new PainRecordDto("SHOULDER", "LEFT", 7),
                new PainRecordDto("KNEE", "RIGHT", 4)
        );
        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), records);
        journalService.createJournal(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getPainRecords())
                .hasSize(2)
                .allMatch(r -> r.getTiming() == PainTiming.PRE);
    }

    @Test
    @DisplayName("존재하지 않는 userId이면 EntityNotFoundException을 던진다")
    void createJournal_unknownUser_throwsEntityNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), null);

        assertThatThrownBy(() -> journalService.createJournal(999L, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("createJournal은 workoutDate를 Entity에 전달하여 저장한다")
    void createJournal_withWorkoutDate_savesDate() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(journalRepository.save(any())).willReturn(stubSavedJournal(date));

        CreateJournalRequest req = new CreateJournalRequest(date, null, validPreCondition(), null);
        journalService.createJournal(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getWorkoutDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("date 지정 조회는 findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc를 호출한다")
    void getMyJournals_withDate_delegatesToFindByDate() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(journalRepository.findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(1L, date))
                .willReturn(List.of());

        List<JournalSummaryDto> result = journalService.getMyJournals(1L, date, null, null);

        assertThat(result).isEmpty();
        verify(journalRepository).findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(1L, date);
    }

    @Test
    @DisplayName("from/to 기간 조회는 findByAuthorIdAndWorkoutDateBetween을 호출한다")
    void getMyJournals_withFromTo_delegatesToBetween() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 30);
        given(journalRepository.findByAuthorIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(1L, from, to))
                .willReturn(List.of());

        List<JournalSummaryDto> result = journalService.getMyJournals(1L, null, from, to);

        assertThat(result).isEmpty();
        verify(journalRepository).findByAuthorIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(1L, from, to);
    }

    @Test
    @DisplayName("createJournal은 userId를 별도 파라미터로 받아 일지를 생성한다")
    void createJournal_userIdAsSeparateParam_persists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("test@example.com", "token", "tester")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), List.of());
        CreateJournalResponse response = journalService.createJournal(1L, req);

        assertThat(response).isNotNull();
        verify(journalRepository).save(any(WorkoutJournal.class));
    }

    private PostConditionDto validPostCondition() {
        PostConditionDto dto = new PostConditionDto();
        dto.setJointMusclePain(6);
        dto.setIntensityFit(7);
        dto.setGoalAchieved(8);
        dto.setDizziness(2);
        dto.setMood(9);
        return dto;
    }

    @Test
    @DisplayName("updatePostCondition은 JournalDetailDto를 반환한다")
    void updatePostCondition_returnsJournalDetailDto() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        JournalDetailDto result = journalService.updatePostCondition(1L, req);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("updatePostCondition에 imageUrls를 전달하면 attachments로 저장된다")
    void updatePostCondition_withImageUrls_savesAttachments() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());
        req.setImageUrls(List.of(
                "https://cdn.ttamtam.app/img0.jpg",
                "https://cdn.ttamtam.app/img1.jpg"
        ));

        journalService.updatePostCondition(1L, req);

        assertThat(journal.getAttachments()).hasSize(2);
        assertThat(journal.getAttachments().get(0).getImageUrl()).isEqualTo("https://cdn.ttamtam.app/img0.jpg");
        assertThat(journal.getAttachments().get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(journal.getAttachments().get(1).getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("updatePostCondition의 painRecords는 PainTiming.POST로 저장된다")
    void updatePostCondition_painRecords_savedAsPostTiming() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());
        req.setPainRecords(List.of(new PainRecordDto("SHOULDER", "LEFT", 5)));

        journalService.updatePostCondition(1L, req);

        assertThat(journal.getPainRecords())
                .hasSize(1)
                .allMatch(r -> r.getTiming() == PainTiming.POST);
    }

    @Test
    @DisplayName("이미 기록 완료된 일지에 updatePostCondition 호출 시 PostAlreadyRecordedException을 던진다")
    void updatePostCondition_alreadyRecorded_throwsConflict() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        journal.applyPostCondition(6, 7, 8, 2, 9, null);
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.updatePostCondition(1L, req))
                .isInstanceOf(PostAlreadyRecordedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 journalId로 updatePostCondition 호출 시 JournalNotFoundException을 던진다")
    void updatePostCondition_unknownJournal_throwsNotFound() {
        given(journalRepository.findById(999L)).willReturn(Optional.empty());

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.updatePostCondition(999L, req))
                .isInstanceOf(JournalNotFoundException.class);
    }

    @Test
    @DisplayName("completeByLookup은 PRE-only 일지를 lookup하여 post 정보를 저장한다")
    void completeByLookup_findsPreOnlyJournal_andFinalizes() {
        User user = new User("a@b.com", "t", "n");
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
                1L, validDate()))
                .willReturn(Optional.of(journal));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());

        JournalDetailDto result = journalService.completeByLookup(1L, req);

        assertThat(result).isNotNull();
        assertThat(journal.getPostRecordedAt()).isNotNull();
    }

    @Test
    @DisplayName("completeByLookup에서 PRE-only 일지가 없으면 JournalNotFoundException을 던진다")
    void completeByLookup_noMatchingJournal_throws404() {
        User user = new User("a@b.com", "t", "n");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
                1L, validDate()))
                .willReturn(Optional.empty());

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.completeByLookup(1L, req))
                .isInstanceOf(JournalNotFoundException.class);
    }

    @Test
    @DisplayName("completeByLookup의 painRecords는 PainTiming.POST로 강제된다")
    void completeByLookup_painRecords_savedAsPostTiming() {
        User user = new User("a@b.com", "t", "n");
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
                1L, validDate()))
                .willReturn(Optional.of(journal));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setPainRecords(List.of(new PainRecordDto("KNEE", "RIGHT", 4)));

        journalService.completeByLookup(1L, req);

        assertThat(journal.getPainRecords())
                .hasSize(1)
                .allMatch(r -> r.getTiming() == PainTiming.POST);
    }

    @Test
    @DisplayName("completeByLookup에서 이미 완료된 일지이면 JournalNotFoundException을 던진다 (lookup 자체가 실패)")
    void completeByLookup_alreadyCompleted_throws404() {
        User user = new User("a@b.com", "t", "n");
        WorkoutJournal completed = stubSavedJournal(validDate());
        completed.applyPostCondition(6, 7, 8, 2, 9, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
                1L, validDate()))
                .willReturn(Optional.empty());

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.completeByLookup(1L, req))
                .isInstanceOf(JournalNotFoundException.class);
    }

    @Test
    @DisplayName("completeByLookup에 존재하지 않는 userId이면 EntityNotFoundException을 던진다")
    void completeByLookup_unknownUser_throwsEntityNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.completeByLookup(999L, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("date와 from/to를 동시 지정하면 IllegalArgumentException을 던진다")
    void getMyJournals_withDateAndRange_throwsBadRequest() {
        LocalDate date = LocalDate.of(2026, 4, 20);
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 30);

        assertThatThrownBy(() -> journalService.getMyJournals(1L, date, from, to))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("from이 to보다 나중이면 IllegalArgumentException을 던진다")
    void getMyJournals_fromAfterTo_throwsBadRequest() {
        LocalDate from = LocalDate.of(2026, 4, 30);
        LocalDate to = LocalDate.of(2026, 4, 1);

        assertThatThrownBy(() -> journalService.getMyJournals(1L, null, from, to))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
