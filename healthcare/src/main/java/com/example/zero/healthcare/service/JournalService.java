package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.journal.BodyPart;
import com.example.zero.healthcare.Entity.journal.BodySide;
import com.example.zero.healthcare.Entity.journal.JournalAttachment;
import com.example.zero.healthcare.Entity.journal.JournalPainRecord;
import com.example.zero.healthcare.Entity.journal.PainTiming;
import com.example.zero.healthcare.Entity.journal.WorkoutJournal;
import com.example.zero.healthcare.dto.journal.CompleteJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalRequest;
import com.example.zero.healthcare.dto.journal.CreateJournalResponse;
import com.example.zero.healthcare.dto.journal.JournalDetailDto;
import com.example.zero.healthcare.dto.journal.JournalSummaryDto;
import com.example.zero.healthcare.dto.journal.PainRecordDto;
import com.example.zero.healthcare.dto.journal.PostConditionDto;
import com.example.zero.healthcare.dto.journal.UpdateJournalPostRequest;
import com.example.zero.healthcare.exception.journal.JournalNotFoundException;
import com.example.zero.healthcare.exception.journal.PostAlreadyRecordedException;
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

    @Transactional
    public CreateJournalResponse createJournal(Long userId, CreateJournalRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        WorkoutJournal journal = WorkoutJournal.builder()
                .folderId(request.getFolderId())
                .authorId(userId)
                .workoutDate(request.getWorkoutDate())
                .preJointMusclePain(request.getPreCondition().getJointMusclePain())
                .preSleepHours(request.getPreCondition().getSleepHours())
                .preSleepQuality(request.getPreCondition().getSleepQuality())
                .prePreviousFatigue(request.getPreCondition().getPreviousFatigue())
                .preOverallCondition(request.getPreCondition().getOverallCondition())
                .build();

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
    public JournalDetailDto updatePostCondition(Long journalId, UpdateJournalPostRequest request) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(JournalNotFoundException::new);

        if (journal.getPostRecordedAt() != null) {
            throw new PostAlreadyRecordedException();
        }

        applyPostFinalize(journal, request.getPostCondition(), request.getPainRecords(),
                request.getContent(), request.getImageUrls());

        return new JournalDetailDto(journal);
    }

    @Transactional
    public JournalDetailDto completeByLookup(Long userId, CompleteJournalRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        WorkoutJournal journal = journalRepository
                .findFirstByAuthorIdAndWorkoutDateAndPostRecordedAtIsNullOrderByCreatedAtDesc(
                        userId, request.getWorkoutDate())
                .orElseThrow(JournalNotFoundException::new);

        applyPostFinalize(journal, request.getPostCondition(), request.getPainRecords(),
                request.getContent(), request.getImageUrls());

        return new JournalDetailDto(journal);
    }

    private void applyPostFinalize(WorkoutJournal journal, PostConditionDto postCondition,
                                   List<PainRecordDto> painRecords, String content,
                                   List<String> imageUrls) {
        journal.applyPostCondition(
                postCondition.getJointMusclePain(),
                postCondition.getIntensityFit(),
                postCondition.getGoalAchieved(),
                postCondition.getDizziness(),
                postCondition.getMood(),
                content
        );

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
    public JournalDetailDto getJournalDetail(Long journalId) {
        WorkoutJournal journal = journalRepository.findById(journalId)
                .orElseThrow(JournalNotFoundException::new);
        return new JournalDetailDto(journal);
    }
}
