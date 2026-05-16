package com.example.zero.healthcare.journal.service;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.example.zero.healthcare.Entity.DiaryFolderMember;
import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.Entity.journal.WorkoutSet;
import com.example.zero.healthcare.dto.journal.CompleteJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.ExerciseDto;
import com.example.zero.healthcare.dto.journal.JournalDetailDto;
import com.example.zero.healthcare.dto.journal.JournalSummaryDto;
import com.example.zero.healthcare.dto.journal.PainRecordDto;
import com.example.zero.healthcare.dto.journal.PostConditionDto;
import com.example.zero.healthcare.dto.journal.PreConditionDto;
import com.example.zero.healthcare.dto.journal.SetDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalRequest;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;
import com.example.zero.healthcare.repository.DiaryFolderMemberRepository;
import com.example.zero.healthcare.repository.DiaryFolderRepository;
import com.example.zero.healthcare.repository.JournalPostConditionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JournalRepository journalRepository;
    @Mock private JournalPostConditionRepository postConditionRepository;
    @Mock private DiaryFolderRepository folderRepository;
    @Mock private DiaryFolderMemberRepository memberRepository;
    @InjectMocks private JournalService journalService;

    private PreConditionDto validPreCondition() {
        return new PreConditionDto(5, 7, 6, 4, 8);
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

    private LocalDate validDate() {
        return LocalDate.of(2026, 4, 20);
    }

    private WorkoutJournal stubSavedJournal(LocalDate workoutDate) {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(workoutDate)
                .build();
        JournalPreCondition pre = JournalPreCondition.of(journal, validPreCondition());
        journal.setPreCondition(pre);
        return journal;
    }

    // ─── createJournal ──────────────────────────────────────────────────────

    @Test
    @DisplayName("유효한 요청으로 일지를 생성하면 journalId와 createdAt을 반환한다")
    void createJournal_withValidRequest_returnsJournalIdAndPersists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("test@example.com", "token", "tester")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), List.of(), null);
        CreateJournalResponse response = journalService.createJournal(1L, req);

        assertThat(response).isNotNull();
        verify(journalRepository).save(any(WorkoutJournal.class));
    }

    @Test
    @DisplayName("painRecords가 빈 배열이면 pain record 없이 일지만 저장된다")
    void createJournal_withEmptyPainRecords_persistsJournalOnly() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), List.of(), null);
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

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), null, null);
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
        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), records, null);
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

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), null, null);

        assertThatThrownBy(() -> journalService.createJournal(999L, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("createJournal은 workoutDate를 Entity에 전달하여 저장한다")
    void createJournal_withWorkoutDate_savesDate() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(journalRepository.save(any())).willReturn(stubSavedJournal(date));

        CreateJournalRequest req = new CreateJournalRequest(date, null, validPreCondition(), null, null);
        journalService.createJournal(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getWorkoutDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("createJournal은 preCondition을 별도 Entity로 설정한다")
    void createJournal_savesPreConditionSeparately() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), null, null);
        journalService.createJournal(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        WorkoutJournal captured = captor.getValue();
        assertThat(captured.getPreCondition()).isNotNull();
        assertThat(captured.getPreCondition().getOverallCondition()).isEqualTo(8);
    }

    @Test
    @DisplayName("createJournal은 userId를 별도 파라미터로 받아 일지를 생성한다")
    void createJournal_userIdAsSeparateParam_persists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("test@example.com", "token", "tester")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), List.of(), null);
        CreateJournalResponse response = journalService.createJournal(1L, req);

        assertThat(response).isNotNull();
        verify(journalRepository).save(any(WorkoutJournal.class));
    }

    // ─── getMyJournals ───────────────────────────────────────────────────────

    @Test
    @DisplayName("date 지정 조회는 findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc를 호출한다")
    void getMyJournals_withDate_delegatesToFindByDate() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(journalRepository.findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(1L, date))
                .willReturn(List.of());

        List<JournalSummaryDto> result = journalService.getMyJournals(1L, date, null, null, null, false);

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

        List<JournalSummaryDto> result = journalService.getMyJournals(1L, null, from, to, null, false);

        assertThat(result).isEmpty();
        verify(journalRepository).findByAuthorIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(1L, from, to);
    }

    @Test
    @DisplayName("date와 from/to를 동시 지정하면 IllegalArgumentException을 던진다")
    void getMyJournals_withDateAndRange_throwsBadRequest() {
        LocalDate date = LocalDate.of(2026, 4, 20);
        LocalDate from = LocalDate.of(2026, 4, 1);
        LocalDate to = LocalDate.of(2026, 4, 30);

        assertThatThrownBy(() -> journalService.getMyJournals(1L, date, from, to, null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("from이 to보다 나중이면 IllegalArgumentException을 던진다")
    void getMyJournals_fromAfterTo_throwsBadRequest() {
        LocalDate from = LocalDate.of(2026, 4, 30);
        LocalDate to = LocalDate.of(2026, 4, 1);

        assertThatThrownBy(() -> journalService.getMyJournals(1L, null, from, to, null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("folderId 지정 조회는 폴더 스코프 Repository 메서드를 호출한다")
    void getMyJournals_folderIdSpecified_callsFolderScopedRepository() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(folderRepository.findById(10L)).willReturn(Optional.of(DiaryFolder.create("test folder")));
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(journalRepository.findByAuthorIdAndFolderIdAndWorkoutDateOrderByCreatedAtDesc(1L, 10L, date))
                .willReturn(List.of());

        List<JournalSummaryDto> result = journalService.getMyJournals(1L, date, null, null, 10L, false);

        assertThat(result).isEmpty();
        verify(journalRepository).findByAuthorIdAndFolderIdAndWorkoutDateOrderByCreatedAtDesc(1L, 10L, date);
    }

    @Test
    @DisplayName("unfiled=true 조회는 folderId IS NULL 스코프 Repository 메서드를 호출한다")
    void getMyJournals_unfiledTrue_callsUnfiledRepository() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        LocalDate date = LocalDate.of(2026, 4, 20);
        given(journalRepository.findByAuthorIdAndFolderIdIsNullAndWorkoutDateOrderByCreatedAtDesc(1L, date))
                .willReturn(List.of());

        List<JournalSummaryDto> result = journalService.getMyJournals(1L, date, null, null, null, true);

        assertThat(result).isEmpty();
        verify(journalRepository).findByAuthorIdAndFolderIdIsNullAndWorkoutDateOrderByCreatedAtDesc(1L, date);
    }

    @Test
    @DisplayName("folderId와 unfiled를 동시 지정하면 IllegalArgumentException을 던진다")
    void getMyJournals_folderIdAndUnfiledTogether_throwsIllegalArgument() {
        assertThatThrownBy(() -> journalService.getMyJournals(1L, null, null, null, 10L, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── getJournalDetail 권한 검증 ──────────────────────────────────────────

    @Test
    @DisplayName("본인 일지 상세 조회 시 JournalDetailDto를 반환한다")
    void getJournalDetail_ownJournal_returnsDetail() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(42L)).willReturn(Optional.of(journal));

        JournalDetailDto result = journalService.getJournalDetail(1L, 42L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자 일지(folderId=null) 조회 시 JournalForbiddenException을 던진다")
    void getJournalDetail_otherUserJournal_noFolder_throwsForbidden() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(42L)).willReturn(Optional.of(journal));

        assertThatThrownBy(() -> journalService.getJournalDetail(2L, 42L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }

    @Test
    @DisplayName("같은 폴더 활성 멤버는 다른 사용자의 일지를 조회할 수 있다")
    void getJournalDetail_otherUserJournal_sameFolderActiveMember_returnsDetail() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L).folderId(10L).workoutDate(validDate())
                .build();
        DiaryFolder folder = DiaryFolder.create("test folder");
        User user2 = new User("other@b.com", "token2", "other");
        DiaryFolderMember activeMember = DiaryFolderMember.join(folder, user2);

        given(journalRepository.findById(42L)).willReturn(Optional.of(journal));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(memberRepository.findByFolderAndUser(folder, user2)).willReturn(Optional.of(activeMember));

        JournalDetailDto result = journalService.getJournalDetail(2L, 42L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("같은 폴더를 나간 멤버는 다른 사용자의 일지를 조회할 수 없다")
    void getJournalDetail_otherUserJournal_sameFolderInactiveMember_throwsForbidden() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L).folderId(10L).workoutDate(validDate())
                .build();
        DiaryFolder folder = DiaryFolder.create("test folder");
        User user2 = new User("other@b.com", "token2", "other");
        DiaryFolderMember inactiveMember = DiaryFolderMember.join(folder, user2);
        inactiveMember.leave();

        given(journalRepository.findById(42L)).willReturn(Optional.of(journal));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(memberRepository.findByFolderAndUser(folder, user2)).willReturn(Optional.of(inactiveMember));

        assertThatThrownBy(() -> journalService.getJournalDetail(2L, 42L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }

    @Test
    @DisplayName("폴더 멤버가 아닌 사용자는 폴더 일지를 조회할 수 없다")
    void getJournalDetail_otherUserJournal_notAMember_throwsForbidden() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L).folderId(10L).workoutDate(validDate())
                .build();
        DiaryFolder folder = DiaryFolder.create("test folder");
        User user2 = new User("other@b.com", "token2", "other");

        given(journalRepository.findById(42L)).willReturn(Optional.of(journal));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(memberRepository.findByFolderAndUser(folder, user2)).willReturn(Optional.empty());

        assertThatThrownBy(() -> journalService.getJournalDetail(2L, 42L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }


    // ─── createJournal 폴더 멤버십 검증 ─────────────────────────────────────

    @Test
    @DisplayName("createJournal - folderId가 있고 폴더 멤버가 아니면 FORBIDDEN을 던진다")
    void createJournal_withFolderIdNotMember_throwsForbidden() {
        User user = new User("a@b.com", "t", "n");
        DiaryFolder folder = DiaryFolder.create("test folder");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(memberRepository.findByFolderAndUser(folder, user)).willReturn(Optional.empty());

        CreateJournalRequest req = new CreateJournalRequest(validDate(), 10L, validPreCondition(), null, null);

        assertThatThrownBy(() -> journalService.createJournal(1L, req))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("createJournal - folderId가 있고 비활성 멤버이면 FORBIDDEN을 던진다")
    void createJournal_withFolderIdInactiveMember_throwsForbidden() {
        User user = new User("a@b.com", "t", "n");
        DiaryFolder folder = DiaryFolder.create("test folder");
        DiaryFolderMember inactiveMember = DiaryFolderMember.join(folder, user);
        inactiveMember.leave();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(memberRepository.findByFolderAndUser(folder, user)).willReturn(Optional.of(inactiveMember));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), 10L, validPreCondition(), null, null);

        assertThatThrownBy(() -> journalService.createJournal(1L, req))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("createJournal - folderId가 있고 활성 멤버이면 성공한다")
    void createJournal_withFolderIdActiveMember_succeeds() {
        User user = new User("a@b.com", "t", "n");
        DiaryFolder folder = DiaryFolder.create("test folder");
        DiaryFolderMember activeMember = DiaryFolderMember.join(folder, user);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(memberRepository.findByFolderAndUser(folder, user)).willReturn(Optional.of(activeMember));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), 10L, validPreCondition(), null, null);

        CreateJournalResponse response = journalService.createJournal(1L, req);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("createJournal - folderId가 없으면 폴더 검증 없이 성공한다")
    void createJournal_withoutFolderId_noFolderCheck_succeeds() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.save(any())).willReturn(stubSavedJournal(validDate()));

        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), null, null);

        CreateJournalResponse response = journalService.createJournal(1L, req);

        assertThat(response).isNotNull();
    }

    // ─── completeByLookup ────────────────────────────────────────────────────

    @Test
    @DisplayName("completeByLookup은 PRE-only 일지를 lookup하여 post 정보를 저장한다")
    void completeByLookup_findsPreOnlyJournal_andFinalizes() {
        User user = new User("a@b.com", "t", "n");
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate()))
                .willReturn(Optional.of(journal));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());

        JournalDetailDto result = journalService.completeByLookup(1L, req);

        assertThat(result).isNotNull();
        assertThat(journal.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("completeByLookup - PRE 일지 없으면 새 일지를 insert하고 완료 상태로 반환한다")
    void completeByLookup_noPreJournal_insertsNewCompletedJournal() {
        User user = new User("a@b.com", "t", "n");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate()))
                .willReturn(Optional.empty());
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());

        JournalDetailDto result = journalService.completeByLookup(1L, req);

        assertThat(result).isNotNull();
        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().isCompleted()).isTrue();
    }

    @Test
    @DisplayName("completeByLookup - PRE 일지 없고 folderId 있으면 folder 포함 새 일지를 insert한다")
    void completeByLookup_noPreJournal_withFolderId_savesWithFolder() {
        User user = new User("a@b.com", "t", "n");
        DiaryFolder folder = DiaryFolder.create("test folder");
        DiaryFolderMember activeMember = DiaryFolderMember.join(folder, user);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(memberRepository.findByFolderAndUser(folder, user)).willReturn(Optional.of(activeMember));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate())).willReturn(Optional.empty());
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setFolderId(10L);

        journalService.completeByLookup(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getFolderId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("completeByLookup - folderId가 있고 멤버가 아니면 FORBIDDEN을 던진다")
    void completeByLookup_noPreJournal_withFolderNotMember_throwsForbidden() {
        User user = new User("a@b.com", "t", "n");
        DiaryFolder folder = DiaryFolder.create("test folder");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(memberRepository.findByFolderAndUser(folder, user)).willReturn(Optional.empty());

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setFolderId(10L);

        assertThatThrownBy(() -> journalService.completeByLookup(1L, req))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("completeByLookup - exercises를 전달하면 운동 기록이 저장된다")
    void completeByLookup_withExercises_savesExercises() {
        User user = new User("a@b.com", "t", "n");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate())).willReturn(Optional.empty());
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("스쿼트");
        exerciseDto.setDisplayOrder(1);
        SetDto setDto = new SetDto();
        setDto.setSetNumber(1);
        setDto.setReps(10);
        setDto.setWeightKg(new BigDecimal("80.0"));
        exerciseDto.setSets(List.of(setDto));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setExercises(List.of(exerciseDto));

        journalService.completeByLookup(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getExercises()).hasSize(1);
        assertThat(captor.getValue().getExercises().get(0).getExerciseName()).isEqualTo("스쿼트");
    }

    @Test
    @DisplayName("소수점 무게(0.5kg)가 WorkoutSet에 정확히 저장된다")
    void completeByLookup_fractionalWeight_savesPrecisely() {
        User user = new User("a@b.com", "t", "n");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate())).willReturn(Optional.empty());
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("덤벨 컬");
        exerciseDto.setDisplayOrder(1);
        SetDto setDto = new SetDto();
        setDto.setSetNumber(1);
        setDto.setReps(12);
        setDto.setWeightKg(new BigDecimal("0.5"));
        exerciseDto.setSets(List.of(setDto));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setExercises(List.of(exerciseDto));

        journalService.completeByLookup(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        WorkoutSet savedSet = captor.getValue().getExercises().get(0).getSets().get(0);
        assertThat(savedSet.getWeightKg()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    @DisplayName("Cardio 운동의 durationMinutes는 WorkoutSet에 별도 필드로 저장된다")
    void completeByLookup_cardioExercise_savesDurationMinutes() {
        User user = new User("a@b.com", "t", "n");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate())).willReturn(Optional.empty());
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("트레드밀");
        exerciseDto.setDisplayOrder(1);
        SetDto setDto = new SetDto();
        setDto.setSetNumber(1);
        setDto.setReps(0);
        setDto.setWeightKg(BigDecimal.ZERO);
        setDto.setDurationMinutes(30);
        exerciseDto.setSets(List.of(setDto));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setExercises(List.of(exerciseDto));

        journalService.completeByLookup(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        WorkoutSet savedSet = captor.getValue().getExercises().get(0).getSets().get(0);
        assertThat(savedSet.getDurationMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("일반 운동의 durationMinutes는 null로 저장된다")
    void completeByLookup_strengthExercise_durationMinutesIsNull() {
        User user = new User("a@b.com", "t", "n");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate())).willReturn(Optional.empty());
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setExerciseName("스쿼트");
        exerciseDto.setDisplayOrder(1);
        SetDto setDto = new SetDto();
        setDto.setSetNumber(1);
        setDto.setReps(10);
        setDto.setWeightKg(new BigDecimal("80.0"));
        exerciseDto.setSets(List.of(setDto));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setExercises(List.of(exerciseDto));

        journalService.completeByLookup(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        WorkoutSet savedSet = captor.getValue().getExercises().get(0).getSets().get(0);
        assertThat(savedSet.getDurationMinutes()).isNull();
    }

    @Test
    @DisplayName("completeByLookup의 painRecords는 PainTiming.POST로 강제된다")
    void completeByLookup_painRecords_savedAsPostTiming() {
        User user = new User("a@b.com", "t", "n");
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate()))
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
    @DisplayName("completeByLookup에 존재하지 않는 userId이면 EntityNotFoundException을 던진다")
    void completeByLookup_unknownUser_throwsEntityNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.completeByLookup(999L, req))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ─── 타이머 필드 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createJournal은 request의 startedAt을 Entity에 저장한다")
    void createJournal_storesStartedAtFromRequest() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        CreateJournalRequest req = new CreateJournalRequest(validDate(), null, validPreCondition(), null, startedAt);
        journalService.createJournal(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    @DisplayName("completeByLookup - 기존 PRE-only 일지의 startedAt은 덮어쓰지 않는다")
    void completeByLookup_existingJournal_preservesStartedAt() {
        LocalDateTime originalStartedAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        WorkoutJournal existingJournal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(validDate())
                .startedAt(originalStartedAt)
                .build();
        JournalPreCondition pre = JournalPreCondition.of(existingJournal, validPreCondition());
        existingJournal.setPreCondition(pre);

        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate())).willReturn(Optional.of(existingJournal));

        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setStartedAt(LocalDateTime.of(2026, 4, 20, 11, 0));
        req.setTotalDurationSeconds(300);

        journalService.completeByLookup(1L, req);

        assertThat(existingJournal.getStartedAt()).isEqualTo(originalStartedAt);
    }

    @Test
    @DisplayName("completeByLookup - 신규 생성 분기는 request의 startedAt을 저장한다")
    void completeByLookup_newJournal_storesStartedAtFromRequest() {
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("a@b.com", "t", "n")));
        given(journalRepository.findFirstPreOnlyJournal(1L, validDate())).willReturn(Optional.empty());
        given(journalRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 20, 9, 0);
        CompleteJournalRequest req = new CompleteJournalRequest();
        req.setWorkoutDate(validDate());
        req.setPostCondition(validPostCondition());
        req.setStartedAt(startedAt);
        req.setTotalDurationSeconds(300);

        journalService.completeByLookup(1L, req);

        ArgumentCaptor<WorkoutJournal> captor = ArgumentCaptor.forClass(WorkoutJournal.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getStartedAt()).isEqualTo(startedAt);
    }

    // ─── deleteJournal ───────────────────────────────────────────────────────

    @Test
    @DisplayName("작성자가 삭제하면 journalRepository.softDeleteById가 호출된다")
    void deleteJournal_byAuthor_callsSoftDeleteById() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        journalService.deleteJournal(1L, 1L);

        verify(journalRepository).softDeleteById(1L);
    }

    @Test
    @DisplayName("폴더 멤버라도 작성자가 아니면 JournalForbiddenException을 던진다")
    void deleteJournal_byNonAuthorFolderMember_throwsForbidden() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L).folderId(10L).workoutDate(validDate())
                .build();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        assertThatThrownBy(() -> journalService.deleteJournal(2L, 1L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }

    @Test
    @DisplayName("무관계 사용자가 삭제 시도하면 JournalForbiddenException을 던진다")
    void deleteJournal_byOtherUser_throwsForbidden() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        assertThatThrownBy(() -> journalService.deleteJournal(2L, 1L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }

    @Test
    @DisplayName("존재하지 않는 journalId로 삭제 시 JournalNotFoundException을 던진다")
    void deleteJournal_nonExistentJournal_throwsNotFound() {
        given(journalRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> journalService.deleteJournal(1L, 999L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_NOT_FOUND));
    }

    // ─── updateJournal ───────────────────────────────────────────────────────

    private UpdateJournalRequest emptyUpdateRequest() {
        return new UpdateJournalRequest();
    }

    private WorkoutJournal stubJournalWithBoth() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(validDate())
                .build();
        JournalPreCondition pre = JournalPreCondition.of(journal, validPreCondition());
        journal.setPreCondition(pre);
        JournalPostCondition post = JournalPostCondition.of(journal, validPostCondition());
        journal.setPostCondition(post);
        return journal;
    }

    private WorkoutJournal stubJournalPreOnly() {
        return stubSavedJournal(validDate());
    }

    private WorkoutJournal stubJournalPostOnly() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L)
                .workoutDate(validDate())
                .build();
        JournalPostCondition post = JournalPostCondition.of(journal, validPostCondition());
        journal.setPostCondition(post);
        return journal;
    }

    @Test
    @DisplayName("일지가 없으면 JournalNotFoundException을 던진다")
    void updateJournal_journalNotFound_throws() {
        given(journalRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> journalService.updateJournal(1L, 999L, emptyUpdateRequest()))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_NOT_FOUND));
    }

    @Test
    @DisplayName("작성자도 아니고 폴더도 없으면 JournalForbiddenException을 던진다")
    void updateJournal_notAuthor_noFolderJournal_throwsForbidden() {
        WorkoutJournal journal = stubSavedJournal(validDate()); // authorId=1
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        assertThatThrownBy(() -> journalService.updateJournal(2L, 1L, emptyUpdateRequest()))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }

    @Test
    @DisplayName("작성자가 아니어도 active 폴더 멤버이면 수정에 성공한다")
    void updateJournal_notAuthor_activeMember_succeeds() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L).folderId(10L).workoutDate(validDate())
                .build();
        DiaryFolder folder = DiaryFolder.create("test folder");
        User user2 = new User("other@b.com", "token2", "other");
        DiaryFolderMember activeMember = DiaryFolderMember.join(folder, user2);

        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(memberRepository.findByFolderAndUser(folder, user2)).willReturn(Optional.of(activeMember));

        JournalDetailDto result = journalService.updateJournal(2L, 1L, emptyUpdateRequest());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("inactive 폴더 멤버이면 수정 시 JournalForbiddenException을 던진다")
    void updateJournal_notAuthor_inactiveMember_throwsForbidden() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L).folderId(10L).workoutDate(validDate())
                .build();
        DiaryFolder folder = DiaryFolder.create("test folder");
        User user2 = new User("other@b.com", "token2", "other");
        DiaryFolderMember inactiveMember = DiaryFolderMember.join(folder, user2);
        inactiveMember.leave();

        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(memberRepository.findByFolderAndUser(folder, user2)).willReturn(Optional.of(inactiveMember));

        assertThatThrownBy(() -> journalService.updateJournal(2L, 1L, emptyUpdateRequest()))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }

    @Test
    @DisplayName("작성자가 아니고 폴더 멤버가 아닌 경우 JournalForbiddenException을 던진다")
    void updateJournal_notAuthor_notMember_throwsForbidden() {
        WorkoutJournal journal = WorkoutJournal.builder()
                .authorId(1L).folderId(10L).workoutDate(validDate())
                .build();
        DiaryFolder folder = DiaryFolder.create("test folder");
        User user2 = new User("other@b.com", "token2", "other");

        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));
        given(folderRepository.findById(10L)).willReturn(Optional.of(folder));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(memberRepository.findByFolderAndUser(folder, user2)).willReturn(Optional.empty());

        assertThatThrownBy(() -> journalService.updateJournal(2L, 1L, emptyUpdateRequest()))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.JOURNAL_FORBIDDEN));
    }

    @Test
    @DisplayName("content만 수정하면 content가 바뀌고 나머지는 그대로다")
    void updateJournal_onlyContent_replacesContent_andOthersUntouched() {
        WorkoutJournal journal = stubJournalWithBoth();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setContent("새 메모");

        journalService.updateJournal(1L, 1L, req);

        assertThat(journal.getContent()).isEqualTo("새 메모");
        assertThat(journal.getPainRecords()).isEmpty();
    }

    @Test
    @DisplayName("content를 빈 문자열로 전송하면 메모가 빈 값으로 저장된다")
    void updateJournal_emptyContent_clearsContent() {
        WorkoutJournal journal = stubJournalWithBoth();
        journal.updateContent("기존 메모");
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setContent("");

        journalService.updateJournal(1L, 1L, req);

        assertThat(journal.getContent()).isEqualTo("");
    }

    @Test
    @DisplayName("모든 필드가 null이면 아무 변경도 없고 touch가 호출되지 않는다")
    void updateJournal_allFieldsNull_noOp_doesNotTouch() {
        WorkoutJournal journal = spy(stubJournalWithBoth());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        journalService.updateJournal(1L, 1L, emptyUpdateRequest());

        verify(journal, never()).touch();
    }

    @Test
    @DisplayName("한 필드라도 변경되면 touch가 호출된다")
    void updateJournal_anyChange_callsTouch() {
        WorkoutJournal journal = spy(stubJournalWithBoth());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setContent("변경");

        journalService.updateJournal(1L, 1L, req);

        verify(journal).touch();
    }

    @Test
    @DisplayName("imageUrls를 전달하면 attachments가 순서대로 교체된다")
    void updateJournal_imageUrls_replacesAttachments_inOrder() {
        WorkoutJournal journal = stubJournalWithBoth();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setImageUrls(List.of("https://cdn.ttamtam.app/a.jpg", "https://cdn.ttamtam.app/b.jpg"));

        journalService.updateJournal(1L, 1L, req);

        assertThat(journal.getAttachments()).hasSize(2);
        assertThat(journal.getAttachments().get(0).getImageUrl()).isEqualTo("https://cdn.ttamtam.app/a.jpg");
        assertThat(journal.getAttachments().get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(journal.getAttachments().get(1).getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("imageUrls를 빈 배열로 전달하면 attachments가 모두 삭제된다")
    void updateJournal_emptyImageUrls_clearsAttachments() {
        WorkoutJournal journal = stubJournalWithBoth();
        journal.addAttachment(JournalAttachment.builder().imageUrl("https://cdn.ttamtam.app/x.jpg").displayOrder(0).build());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setImageUrls(List.of());

        journalService.updateJournal(1L, 1L, req);

        assertThat(journal.getAttachments()).isEmpty();
    }

    @Test
    @DisplayName("pre 데이터 없는 일지에 prePainRecords 전달 시 PreNotRecordedException을 던진다")
    void updateJournal_prePainRecords_butNoPre_throwsPreNotRecorded() {
        WorkoutJournal journal = stubJournalPostOnly();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPrePainRecords(List.of(new PainRecordDto("SHOULDER", "LEFT", 5)));

        assertThatThrownBy(() -> journalService.updateJournal(1L, 1L, req))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.PRE_NOT_RECORDED));
    }

    @Test
    @DisplayName("prePainRecords를 교체해도 POST 통증은 유지된다")
    void updateJournal_prePainRecords_replacesPRE_keepsPOST() {
        WorkoutJournal journal = stubJournalWithBoth();
        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.POST).bodyPart(BodyPart.KNEE).side(BodySide.RIGHT).painLevel(3).build());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPrePainRecords(List.of(new PainRecordDto("SHOULDER", "LEFT", 7)));

        journalService.updateJournal(1L, 1L, req);

        long preCount = journal.getPainRecords().stream().filter(r -> r.getTiming() == PainTiming.PRE).count();
        long postCount = journal.getPainRecords().stream().filter(r -> r.getTiming() == PainTiming.POST).count();
        assertThat(preCount).isEqualTo(1);
        assertThat(postCount).isEqualTo(1);
    }

    @Test
    @DisplayName("post 데이터 없는 일지에 postPainRecords 전달 시 PostNotRecordedException을 던진다")
    void updateJournal_postPainRecords_butNoPost_throwsPostNotRecorded() {
        WorkoutJournal journal = stubJournalPreOnly();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPostPainRecords(List.of(new PainRecordDto("KNEE", "RIGHT", 4)));

        assertThatThrownBy(() -> journalService.updateJournal(1L, 1L, req))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.POST_NOT_RECORDED));
    }

    @Test
    @DisplayName("postPainRecords를 교체해도 PRE 통증은 유지된다")
    void updateJournal_postPainRecords_replacesPOST_keepsPRE() {
        WorkoutJournal journal = stubJournalWithBoth();
        journal.addPainRecord(JournalPainRecord.builder()
                .timing(PainTiming.PRE).bodyPart(BodyPart.SHOULDER).side(BodySide.LEFT).painLevel(5).build());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPostPainRecords(List.of(new PainRecordDto("KNEE", "RIGHT", 3)));

        journalService.updateJournal(1L, 1L, req);

        long preCount = journal.getPainRecords().stream().filter(r -> r.getTiming() == PainTiming.PRE).count();
        long postCount = journal.getPainRecords().stream().filter(r -> r.getTiming() == PainTiming.POST).count();
        assertThat(preCount).isEqualTo(1);
        assertThat(postCount).isEqualTo(1);
    }

    @Test
    @DisplayName("통증 기록에 같은 bodyPart+side 중복이 있으면 IllegalArgumentException을 던진다")
    void updateJournal_painRecords_duplicateBodyPartAndSide_throwsBadRequest() {
        WorkoutJournal journal = stubJournalWithBoth();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPrePainRecords(List.of(
                new PainRecordDto("SHOULDER", "LEFT", 5),
                new PainRecordDto("SHOULDER", "LEFT", 7)
        ));

        assertThatThrownBy(() -> journalService.updateJournal(1L, 1L, req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("pre 데이터 없는 일지에 preCondition 전달 시 PreNotRecordedException을 던진다")
    void updateJournal_preCondition_butNoPre_throwsPreNotRecorded() {
        WorkoutJournal journal = stubJournalPostOnly();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPreCondition(validPreCondition());

        assertThatThrownBy(() -> journalService.updateJournal(1L, 1L, req))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.PRE_NOT_RECORDED));
    }

    @Test
    @DisplayName("preCondition 수정 시 모든 필드가 갱신된다")
    void updateJournal_preCondition_updatesValues() {
        WorkoutJournal journal = stubJournalPreOnly();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        PreConditionDto newDto = new PreConditionDto(1, 2, 3, 4, 5);
        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPreCondition(newDto);

        journalService.updateJournal(1L, 1L, req);

        assertThat(journal.getPreCondition().getJointMusclePain()).isEqualTo(1);
        assertThat(journal.getPreCondition().getSleepHours()).isEqualTo(2);
        assertThat(journal.getPreCondition().getOverallCondition()).isEqualTo(5);
    }

    @Test
    @DisplayName("post 데이터 없는 일지에 postCondition 전달 시 PostNotRecordedException을 던진다")
    void updateJournal_postCondition_butNoPost_throwsPostNotRecorded() {
        WorkoutJournal journal = stubJournalPreOnly();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.updateJournal(1L, 1L, req))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorCode()).isEqualTo(ErrorCode.POST_NOT_RECORDED));
    }

    @Test
    @DisplayName("postCondition 수정 시 값은 갱신되고 recordedAt은 변경되지 않는다")
    void updateJournal_postCondition_updatesValues_keepsRecordedAt() {
        WorkoutJournal journal = stubJournalWithBoth();
        java.time.LocalDateTime originalRecordedAt = journal.getPostCondition().getRecordedAt();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        PostConditionDto newDto = new PostConditionDto();
        newDto.setJointMusclePain(1);
        newDto.setIntensityFit(2);
        newDto.setGoalAchieved(3);
        newDto.setDizziness(4);
        newDto.setMood(5);
        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPostCondition(newDto);

        journalService.updateJournal(1L, 1L, req);

        assertThat(journal.getPostCondition().getMood()).isEqualTo(5);
        assertThat(journal.getPostCondition().getRecordedAt()).isEqualTo(originalRecordedAt);
    }

    @Test
    @DisplayName("post only 일지에 post 필드만 전달하면 정상 처리된다")
    void updateJournal_postOnlyJournal_canEditPostOnly() {
        WorkoutJournal journal = stubJournalPostOnly();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPostCondition(validPostCondition());

        JournalDetailDto result = journalService.updateJournal(1L, 1L, req);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("pre only 일지에 pre 필드만 전달하면 정상 처리된다")
    void updateJournal_preOnlyJournal_canEditPreOnly() {
        WorkoutJournal journal = stubJournalPreOnly();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setPreCondition(validPreCondition());

        JournalDetailDto result = journalService.updateJournal(1L, 1L, req);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("모든 필드를 한번에 수정할 수 있다")
    void updateJournal_allFieldsAtOnce_appliesAll() {
        WorkoutJournal journal = stubJournalWithBoth();
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalRequest req = new UpdateJournalRequest();
        req.setContent("전체 수정");
        req.setPreCondition(new PreConditionDto(1, 1, 1, 1, 1));
        req.setPostCondition(validPostCondition());
        req.setPrePainRecords(List.of(new PainRecordDto("SHOULDER", "LEFT", 5)));
        req.setPostPainRecords(List.of(new PainRecordDto("KNEE", "RIGHT", 3)));
        req.setImageUrls(List.of("https://cdn.ttamtam.app/img.jpg"));

        JournalDetailDto result = journalService.updateJournal(1L, 1L, req);

        assertThat(result).isNotNull();
        assertThat(journal.getContent()).isEqualTo("전체 수정");
        assertThat(journal.getPreCondition().getJointMusclePain()).isEqualTo(1);
        assertThat(journal.getPainRecords()).hasSize(2);
        assertThat(journal.getAttachments()).hasSize(1);
    }

}
