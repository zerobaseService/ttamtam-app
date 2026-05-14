package com.example.zero.healthcare.exercise.service;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavorite;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavoriteId;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import com.example.zero.healthcare.repository.UserExerciseFavoriteRepository;
import com.example.zero.healthcare.service.FavoriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private ExerciseMasterRepository exerciseRepository;

    @Mock
    private UserExerciseFavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService service;

    @Test
    @DisplayName("존재하지 않는 운동에 즐겨찾기 추가 시 CoreException이 발생한다")
    void add_unknownExercise_throwsExerciseNotFound() {
        when(exerciseRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(1L, "UNKNOWN"))
                .isInstanceOf(CoreException.class);

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 즐겨찾기된 운동에 다시 추가해도 예외 없이 무시된다 (멱등)")
    void add_alreadyFavorited_silentlySucceeds() {
        ExerciseMaster exercise = new ExerciseMaster();
        exercise.setId("Bench_Press");
        when(exerciseRepository.findById("Bench_Press")).thenReturn(Optional.of(exercise));
        when(favoriteRepository.existsByIdUserIdAndIdExerciseId(1L, "Bench_Press")).thenReturn(true);

        service.add(1L, "Bench_Press");

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    @DisplayName("즐겨찾기에 없는 항목을 삭제해도 예외 없이 무시된다 (멱등)")
    void remove_notFavorited_silentlySucceeds() {
        when(favoriteRepository.existsByIdUserIdAndIdExerciseId(1L, "Bench_Press")).thenReturn(false);

        service.remove(1L, "Bench_Press");

        verify(favoriteRepository, never()).deleteByIdUserIdAndIdExerciseId(any(), any());
    }

    @Test
    @DisplayName("listIds는 사용자의 즐겨찾기 exerciseId 목록을 반환한다")
    void listIds_returnsUserOwnFavoriteIds() {
        UserExerciseFavorite fav1 = new UserExerciseFavorite();
        fav1.setId(new UserExerciseFavoriteId(1L, "Bench_Press"));
        fav1.setCreatedAt(LocalDateTime.now());

        UserExerciseFavorite fav2 = new UserExerciseFavorite();
        fav2.setId(new UserExerciseFavoriteId(1L, "Deadlift"));
        fav2.setCreatedAt(LocalDateTime.now());

        when(favoriteRepository.findAllByIdUserId(1L)).thenReturn(List.of(fav1, fav2));

        List<String> result = service.listIds(1L);

        assertThat(result).containsExactlyInAnyOrder("Bench_Press", "Deadlift");
    }
}
