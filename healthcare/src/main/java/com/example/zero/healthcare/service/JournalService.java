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

    @Transactional
    public CreateJournalResponse createJournal(Long userId, CreateJournalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

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

        JournalPreCondition pre = JournalPreCondition.of(journal, request.getPreCondition());
        journal.setPreCondition(pre);

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

        applyPostFinalize(journal, request.getPostCondition(), request.getPainRecords(),
                request.getContent(), request.getImageUrls(), request.getTotalDurationSeconds());

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

        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                journal.addAttachment(JournalAttachment.builder()
                        .imageUrl(imageUrls.get(i))
                        .displayOrder(i)
                        .build());
            }
        }
    }

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

        return journals.stream()
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

    @Transactional
    public void deleteJournal(Long userId, Long journalId) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_NOT_FOUND));
        if (!journal.getAuthorId().equals(userId)) {
            throw new CoreException(ErrorCode.JOURNAL_FORBIDDEN);
        }
        journalRepository.softDeleteById(journalId);
    }

    @Transactional
    public JournalDetailDto updateJournal(Long userId, Long journalId, UpdateJournalRequest request) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_NOT_FOUND));
        verifyJournalAccess(journal, userId);
        applyUpdate(journal, request);
        return new JournalDetailDto(journal);
    }

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

    private void verifyJournalAccess(WorkoutJournal journal, Long userId) {
        if (journal.getAuthorId().equals(userId)) {
            return;
        }
        if (journal.getFolderId() != null) {
            DiaryFolder folder = folderRepository.findById(journal.getFolderId())
                    .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_FORBIDDEN));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CoreException(ErrorCode.JOURNAL_FORBIDDEN));
            boolean isActiveMember = memberRepository.findByFolderAndUser(folder, user)
                    .map(DiaryFolderMember::isActive)
                    .orElse(false);
            if (isActiveMember) {
                return;
            }
        }
        throw new CoreException(ErrorCode.JOURNAL_FORBIDDEN);
    }
}
