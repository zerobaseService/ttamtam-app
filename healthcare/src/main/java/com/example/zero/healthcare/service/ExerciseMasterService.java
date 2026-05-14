package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.dto.exercise.ExerciseDetailDto;
import com.example.zero.healthcare.dto.exercise.ExerciseSummaryDto;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import com.example.zero.healthcare.repository.UserExerciseFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseMasterService {

    private final ExerciseMasterRepository repository;
    private final UserExerciseFavoriteRepository favoriteRepository;

    public List<ExerciseSummaryDto> getAll(Long userId) {
        Set<String> favoriteIds = favoriteRepository.findAllByIdUserId(userId).stream()
                .map(f -> f.getId().getExerciseId())
                .collect(Collectors.toSet());

        return repository.findAll().stream()
                .sorted(Comparator
                        .comparing((ExerciseMaster e) -> !favoriteIds.contains(e.getId()))
                        .thenComparing(e -> e.getKoreanName() != null ? e.getKoreanName() : e.getName()))
                .map(e -> new ExerciseSummaryDto(e, favoriteIds.contains(e.getId())))
                .toList();
    }

    public ExerciseDetailDto getById(String id) {
        ExerciseMaster exercise = repository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorCode.EXERCISE_NOT_FOUND));
        return new ExerciseDetailDto(exercise);
    }
}
