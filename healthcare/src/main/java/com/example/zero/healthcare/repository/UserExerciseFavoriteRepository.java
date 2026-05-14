package com.example.zero.healthcare.repository;

import com.example.zero.healthcare.Entity.exercise.UserExerciseFavorite;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserExerciseFavoriteRepository extends JpaRepository<UserExerciseFavorite, UserExerciseFavoriteId> {

    List<UserExerciseFavorite> findAllByIdUserId(Long userId);

    boolean existsByIdUserIdAndIdExerciseId(Long userId, String exerciseId);

    void deleteByIdUserIdAndIdExerciseId(Long userId, String exerciseId);
}
