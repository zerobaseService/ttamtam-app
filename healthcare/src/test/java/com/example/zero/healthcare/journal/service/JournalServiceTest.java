package com.example.zero.healthcare.journal.service;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.example.zero.healthcare.Entity.DiaryFolderMember;
import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
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
import com.example.zero.healthcare.dto.journal.UpdateJournalPostRequest;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;
import com.example.zero.healthcare.exception.journal.JournalForbiddenException;
import com.example.zero.healthcare.exception.journal.JournalNotFoundException;
import com.example.zero.healthcare.exception.journal.PostAlreadyRecordedException;
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

    // ─── updatePostCondition ─────────────────────────────────────────────────

    @Test
    @DisplayName("updatePostCondition은 JournalDetailDto를 반환한다")
    void updatePostCondition_returnsJournalDetailDto() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        JournalDetailDto result = journalService.updatePostCondition(1L, 1L, req);

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

        journalService.updatePostCondition(1L, 1L, req);

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

        journalService.updatePostCondition(1L, 1L, req);

        assertThat(journal.getPainRecords())
                .hasSize(1)
                .allMatch(r -> r.getTiming() == PainTiming.POST);
    }

    @Test
    @DisplayName("이미 기록 완료된 일지에 updatePostCondition 호출 시 PostAlreadyRecordedException을 던진다")
    void updatePostCondition_alreadyRecorded_throwsConflict() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        journal.setPostCondition(JournalPostCondition.of(journal, validPostCondition()));
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.updatePostCondition(1L, 1L, req))
                .isInstanceOf(PostAlreadyRecordedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 journalId로 updatePostCondition 호출 시 JournalNotFoundException을 던진다")
    void updatePostCondition_unknownJournal_throwsNotFound() {
        given(journalRepository.findById(999L)).willReturn(Optional.empty());

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.updatePostCondition(1L, 999L, req))
                .isInstanceOf(JournalNotFoundException.class);
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
                .isInstanceOf(JournalForbiddenException.class);
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
                .isInstanceOf(JournalForbiddenException.class);
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
                .isInstanceOf(JournalForbiddenException.class);
    }

    // ─── updatePostCondition 권한 검증 ───────────────────────────────────────

    @Test
    @DisplayName("다른 사용자 일지(폴더 없음)에 updatePostCondition 시 JournalForbiddenException을 던진다")
    void updatePostCondition_otherUser_noFolder_throwsForbidden() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.updatePostCondition(2L, 1L, req))
                .isInstanceOf(JournalForbiddenException.class);
    }

    @Test
    @DisplayName("같은 폴더 활성 멤버는 다른 사용자의 일지에 updatePostCondition 가능")
    void updatePostCondition_otherUser_sameFolderActiveMember_succeeds() {
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

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        JournalDetailDto result = journalService.updatePostCondition(2L, 1L, req);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("같은 폴더를 나간 멤버는 다른 사용자의 일지에 updatePostCondition 불가")
    void updatePostCondition_otherUser_sameFolderInactiveMember_throwsForbidden() {
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

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());

        assertThatThrownBy(() -> journalService.updatePostCondition(2L, 1L, req))
                .isInstanceOf(JournalForbiddenException.class);
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

    @Test
    @DisplayName("updatePostCondition은 totalDurationSeconds를 Journal에 기록한다")
    void updatePostCondition_recordsTotalDurationSeconds() {
        WorkoutJournal journal = stubSavedJournal(validDate());
        given(journalRepository.findById(1L)).willReturn(Optional.of(journal));

        UpdateJournalPostRequest req = new UpdateJournalPostRequest();
        req.setPostCondition(validPostCondition());
        req.setTotalDurationSeconds(300);

        journalService.updatePostCondition(1L, 1L, req);

        assertThat(journal.getTotalDurationSeconds()).isEqualTo(300);
    }

}
