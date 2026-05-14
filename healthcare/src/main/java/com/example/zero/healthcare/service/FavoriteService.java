package com.example.zero.healthcare.service;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavorite;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavoriteId;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.exception.common.ErrorCode;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import com.example.zero.healthcare.repository.UserExerciseFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final ExerciseMasterRepository exerciseRepository;
    private final UserExerciseFavoriteRepository favoriteRepository;

    @Transactional
    public void add(Long userId, String exerciseId) {
        ExerciseMaster exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new CoreException(ErrorCode.EXERCISE_NOT_FOUND));
        if (favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, exerciseId)) {
            return;
        }
        UserExerciseFavorite fav = new UserExerciseFavorite();
        fav.setId(new UserExerciseFavoriteId(userId, exerciseId));
        fav.setExercise(exercise);
        fav.setCreatedAt(LocalDateTime.now());
        favoriteRepository.save(fav);
    }

    @Transactional
    public void remove(Long userId, String exerciseId) {
        if (!favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, exerciseId)) {
            return;
        }
        favoriteRepository.deleteByIdUserIdAndIdExerciseId(userId, exerciseId);
    }

    @Transactional(readOnly = true)
    public List<String> listIds(Long userId) {
        return favoriteRepository.findAllByIdUserId(userId).stream()
                .map(fav -> fav.getId().getExerciseId())
                .toList();
    }
}
