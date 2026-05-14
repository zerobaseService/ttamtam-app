package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.DiaryFolder;
import com.example.zero.healthcare.Entity.DiaryFolderMember;
import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.JournalPostCondition;
import com.example.zero.healthcare.Entity.journal.JournalPreCondition;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.journal.WorkoutExercise;
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
import com.example.zero.healthcare.dto.journal.SetDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalRequest;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;
import com.example.zero.healthcare.repository.DiaryFolderMemberRepository;
import com.example.zero.healthcare.repository.DiaryFolderRepository;
import com.example.zero.healthcare.repository.JournalPostConditionRepository;
import com.example.zero.healthcare.repository.JournalRepository;
import com.example.zero.healthcare.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final UserRepository userRepository;
    private final JournalRepository journalRepository;
    private final JournalPostConditionRepository postConditionRepository;
    private final DiaryFolderRepository folderRepository;
    private final DiaryFolderMemberRepository memberRepository;

    // [일지 시작] 운동 전 상태(preCondition + PRE 통증)만 기록하고 저장한다.
    // 운동이 끝나면 completeByLookup()에서 POST 데이터를 채워 완성한다.
    @Transactional
    public CreateJournalResponse createJournal(Long userId, CreateJournalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        // 폴더가 지정된 경우 활성 멤버인지 확인 (탈퇴·CLOSED 폴더면 FORBIDDEN)
        if (request.getFolderId() != null) {
            DiaryFolder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new CoreException(ErrorCode.FOLDER_NOT_FOUND));
            memberRepository.findByFolderAndUser(folder, user)
                    .filter(DiaryFolderMember::isActive)
                    .orElseThrow(() -> new CoreException(ErrorCode.FORBIDDEN));
        }

        WorkoutJournal journal = WorkoutJournal.builder()
                .folderId(request.getFolderId())
                .authorId(userId)
                .workoutDate(request.getWorkoutDate())
                .startedAt(request.getStartedAt())
                .build();

        // 운동 전 컨디션(피로도·수면 등)을 preCondition 엔티티로 묶어 연결
        JournalPreCondition pre = JournalPreCondition.of(journal, request.getPreCondition());
        journal.setPreCondition(pre);

        // 운동 전 통증 기록 (timing = PRE)
        List<PainRecordDto> painRecords = request.getPainRecords();
        if (painRecords != null) {
            for (PainRecordDto dto : painRecords) {
                journal.addPainRecord(JournalPainRecord.builder()
                        .timing(PainTiming.PRE)
                        .bodyPart(BodyPart.valueOf(dto.getBodyPart()))
                        .side(BodySide.valueOf(dto.getSide()))
                        .painLevel(dto.getPainLevel())
                        .build());
            }
        }

        WorkoutJournal saved = journalRepository.save(journal);
        return new CreateJournalResponse(saved.getId(), saved.getCreatedAt());
    }

    // [일지 완료] 운동 후 POST 데이터(컨디션·운동 기록·이미지 등)를 채워 일지를 완성한다.
    // 같은 날 PRE-only 일지가 있으면 그것을 재사용하고, 없으면 새 일지를 즉시 생성한다.
    // (createJournal을 건너뛰고 운동 완료 시점에 한 번에 저장하는 경우를 지원하기 위함)
    @Transactional
    public JournalDetailDto completeByLookup(Long userId, CompleteJournalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (request.getFolderId() != null) {
            DiaryFolder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new CoreException(ErrorCode.FOLDER_NOT_FOUND));
            memberRepository.findByFolderAndUser(folder, user)
                    .filter(DiaryFolderMember::isActive)
                    .orElseThrow(() -> new CoreException(ErrorCode.FORBIDDEN));
        }

        // 해당 날짜의 미완료(PRE-only) 일지를 조회 → 없으면 빈 일지 신규 생성
        WorkoutJournal journal = journalRepository
                .findFirstPreOnlyJournal(userId, request.getWorkoutDate())
                .orElseGet(() -> {
                    WorkoutJournal newJournal = WorkoutJournal.builder()
                            .authorId(userId)
                            .workoutDate(request.getWorkoutDate())
                            .folderId(request.getFolderId())
                            .startedAt(request.getStartedAt())
                            .build();
                    return journalRepository.save(newJournal);
                });

        // POST 공통 필드(컨디션·통증·내용·이미지·총 운동 시간)를 일괄 적용
        applyPostFinalize(journal, request.getPostCondition(), request.getPainRecords(),
                request.getContent(), request.getImageUrls(), request.getTotalDurationSeconds());
        if (request.getWorkoutType() != null) {
            journal.updateWorkoutType(request.getWorkoutType());
        }

        // 운동 종목과 세트 기록 추가 (displayOrder 기준으로 정렬 표시됨)
        List<ExerciseDto> exercises = request.getExercises();
        if (exercises != null) {
            for (ExerciseDto exerciseDto : exercises) {
                WorkoutExercise exercise = WorkoutExercise.builder()
                        .exerciseName(exerciseDto.getExerciseName())
                        .displayOrder(exerciseDto.getDisplayOrder() != null ? exerciseDto.getDisplayOrder() : 0)
                        .build();
                if (exerciseDto.getSets() != null) {
                    for (SetDto setDto : exerciseDto.getSets()) {
                        exercise.addSet(WorkoutSet.builder()
                                .setNumber(setDto.getSetNumber())
                                .reps(setDto.getReps())
                                .weightKg(setDto.getWeightKg())
                                .build());
                    }
                }
                journal.addExercise(exercise);
            }
        }

        return new JournalDetailDto(journal);
    }

    // completeByLookup의 POST 공통 처리를 분리한 내부 헬퍼.
    // postCondition은 별도 테이블이라 명시적 save()가 필요 (cascade 미적용)
    private void applyPostFinalize(WorkoutJournal journal, PostConditionDto postCondition,
                                   List<PainRecordDto> painRecords, String content,
                                   List<String> imageUrls, Integer totalDurationSeconds) {
        journal.recordDuration(totalDurationSeconds);
        JournalPostCondition post = JournalPostCondition.of(journal, postCondition);
        journal.setPostCondition(post);
        postConditionRepository.save(post);

        if (content != null) {
            journal.updateContent(content);
        }

        // 운동 후 통증 기록 (timing = POST)
        if (painRecords != null) {
            for (PainRecordDto dto : painRecords) {
                journal.addPainRecord(JournalPainRecord.builder()
                        .timing(PainTiming.POST)
                        .bodyPart(BodyPart.valueOf(dto.getBodyPart()))
                        .side(BodySide.valueOf(dto.getSide()))
                        .painLevel(dto.getPainLevel())
                        .build());
            }
        }

        // 이미지 순서는 리스트 인덱스를 displayOrder로 사용
        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                journal.addAttachment(JournalAttachment.builder()
                        .imageUrl(imageUrls.get(i))
                        .displayOrder(i)
                        .build());
            }
        }
    }

    // [일지 목록 조회] date(단일 날짜) 또는 from~to(범위) 또는 전체 중 하나만 선택 가능.
    // CLOSED 폴더의 일지는 필터링해 노출하지 않는다 (폴더 정책 준수).
    @Transactional(readOnly = true)
    public List<JournalSummaryDto> getMyJournals(Long userId, LocalDate date, LocalDate from, LocalDate to) {
        if (date != null && (from != null || to != null)) {
            throw new IllegalArgumentException("date와 from/to를 동시에 지정할 수 없습니다.");
        }
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("from과 to는 함께 지정해야 합니다.");
        }
        if (from != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to보다 이전이어야 합니다.");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        List<WorkoutJournal> journals;
        if (date != null) {
            journals = journalRepository.findByAuthorIdAndWorkoutDateOrderByCreatedAtDesc(userId, date);
        } else if (from != null) {
            journals = journalRepository.findByAuthorIdAndWorkoutDateBetweenOrderByWorkoutDateDescCreatedAtDesc(userId, from, to);
        } else {
            journals = journalRepository.findByAuthorIdOrderByWorkoutDateDescCreatedAtDesc(userId);
        }

        // 폴더 없는 일지는 항상 노출, 폴더 있는 일지는 해당 폴더가 활성 상태일 때만 노출
        return journals.stream()
                .filter(j -> {
                    if (j.getFolderId() == null) return true;
                    return folderRepository.findById(j.getFolderId())
                            .map(DiaryFolder::isActive)
                            .orElse(false);
                })
                .map(JournalSummaryDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JournalDetailDto getJournalDetail(Long userId, Long journalId) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_NOT_FOUND));
        verifyJournalAccess(journal, userId);
        return new JournalDetailDto(journal);
    }

    // 소프트 딜리트: DB에서 실제 삭제하지 않고 deleted_at을 마킹한다 (복구 가능성 유지)
    // 폴더 멤버여도 타인의 일지는 삭제 불가 — 작성자 본인만 허용
    @Transactional
    public void deleteJournal(Long userId, Long journalId) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_NOT_FOUND));
        if (!journal.getAuthorId().equals(userId)) {
            throw new CoreException(ErrorCode.JOURNAL_FORBIDDEN);
        }
        journalRepository.softDeleteById(journalId);
    }

    // [일지 수정] null이 아닌 필드만 선택적으로 업데이트 (PATCH 방식)
    @Transactional
    public JournalDetailDto updateJournal(Long userId, Long journalId, UpdateJournalRequest request) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_NOT_FOUND));
        verifyJournalAccess(journal, userId);
        applyUpdate(journal, request);
        return new JournalDetailDto(journal);
    }

    // 실제로 변경된 필드가 하나라도 있을 때만 touch()로 updatedAt을 갱신한다
    private void applyUpdate(WorkoutJournal journal, UpdateJournalRequest request) {
        boolean changed = false;
        if (request.getPreCondition() != null) {
            updatePreCondition(journal, request.getPreCondition());
            changed = true;
        }
        if (request.getPostCondition() != null) {
            updatePostConditionField(journal, request.getPostCondition());
            changed = true;
        }
        if (request.getPrePainRecords() != null) {
            replacePainRecords(journal, PainTiming.PRE, request.getPrePainRecords());
            changed = true;
        }
        if (request.getPostPainRecords() != null) {
            replacePainRecords(journal, PainTiming.POST, request.getPostPainRecords());
            changed = true;
        }
        if (request.getContent() != null) {
            journal.updateContent(request.getContent());
            changed = true;
        }
        if (request.getImageUrls() != null) {
            journal.replaceAttachments(request.getImageUrls());
            changed = true;
        }
        if (changed) {
            journal.touch();
        }
    }

    private void updatePreCondition(WorkoutJournal journal, com.example.zero.healthcare.dto.journal.PreConditionDto dto) {
        if (journal.getPreCondition() == null) {
            throw new CoreException(ErrorCode.PRE_NOT_RECORDED);
        }
        journal.getPreCondition().update(dto);
    }

    private void updatePostConditionField(WorkoutJournal journal, PostConditionDto dto) {
        if (journal.getPostCondition() == null) {
            throw new CoreException(ErrorCode.POST_NOT_RECORDED);
        }
        journal.getPostCondition().update(dto);
    }

    private void replacePainRecords(WorkoutJournal journal, PainTiming timing, List<PainRecordDto> dtos) {
        if (timing == PainTiming.PRE && journal.getPreCondition() == null) {
            throw new CoreException(ErrorCode.PRE_NOT_RECORDED);
        }
        if (timing == PainTiming.POST && journal.getPostCondition() == null) {
            throw new CoreException(ErrorCode.POST_NOT_RECORDED);
        }
        validateNoDuplicatePainRecords(dtos);

        // UNIQUE(journal_id, timing, body_part, side) 제약 위반 방지:
        // Hibernate는 INSERT를 DELETE보다 먼저 실행하므로, 삭제 후 flush()로 DELETE를 선행시킨다.
        journal.getPainRecords().removeIf(r -> r.getTiming() == timing);
        journalRepository.flush();

        dtos.stream()
                .map(dto -> JournalPainRecord.builder()
                        .timing(timing)
                        .bodyPart(BodyPart.valueOf(dto.getBodyPart()))
                        .side(BodySide.valueOf(dto.getSide()))
                        .painLevel(dto.getPainLevel())
                        .build())
                .forEach(journal::addPainRecord);
    }

    private void validateNoDuplicatePainRecords(List<PainRecordDto> dtos) {
        long distinct = dtos.stream()
                .map(dto -> dto.getBodyPart() + "|" + dto.getSide())
                .distinct()
                .count();
        if (distinct < dtos.size()) {
            throw new IllegalArgumentException("동일한 신체 부위와 방향의 통증 기록이 중복되었습니다.");
        }
    }

    // 접근 권한 검사: 폴더 일지는 CLOSED 폴더면 차단, 작성자 또는 활성 폴더 멤버면 허용.
    // 폴더 없는 일지는 작성자 본인만 접근 가능.
    private void verifyJournalAccess(WorkoutJournal journal, Long userId) {
        if (journal.getFolderId() != null) {
            DiaryFolder folder = folderRepository.findById(journal.getFolderId())
                    .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_FORBIDDEN));
            if (!folder.isActive()) throw new CoreException(ErrorCode.FOLDER_CLOSED);
            if (journal.getAuthorId().equals(userId)) return;
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_FORBIDDEN));
            boolean isActiveMember = memberRepository.findByFolderAndUser(folder, user)
                    .map(DiaryFolderMember::isActive)
                    .orElse(false);
            if (isActiveMember) return;
            throw new CoreException(ErrorCode.JOURNAL_FORBIDDEN);
        }
        if (journal.getAuthorId().equals(userId)) return;
        throw new CoreException(ErrorCode.JOURNAL_FORBIDDEN);
    }
}
