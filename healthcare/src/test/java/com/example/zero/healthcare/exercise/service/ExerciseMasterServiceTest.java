package com.example.zero.healthcare.exercise.service;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavorite;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavoriteId;
import com.example.zero.healthcare.dto.exercise.ExerciseDetailDto;
import com.example.zero.healthcare.dto.exercise.ExerciseSummaryDto;
import com.example.zero.healthcare.exception.CoreException;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import com.example.zero.healthcare.repository.UserExerciseFavoriteRepository;
import com.example.zero.healthcare.service.ExerciseMasterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseMasterServiceTest {

    @Mock
    private ExerciseMasterRepository repository;

    @Mock
    private UserExerciseFavoriteRepository favoriteRepository;

    @InjectMocks
    private ExerciseMasterService service;

    private ExerciseMaster buildExercise(String id, String name, String koreanName) {
        ExerciseMaster e = new ExerciseMaster();
        e.setId(id);
        e.setName(name);
        e.setKoreanName(koreanName);
        e.setLevel("beginner");
        e.setCategory("strength");
        e.setEquipment("barbell");
        e.setPrimaryMuscles(Set.of("chest"));
        e.setSecondaryMuscles(Set.of());
        return e;
    }

    private UserExerciseFavorite buildFavorite(Long userId, String exerciseId) {
        UserExerciseFavorite fav = new UserExerciseFavorite();
        fav.setId(new UserExerciseFavoriteId(userId, exerciseId));
        fav.setCreatedAt(LocalDateTime.now());
        return fav;
    }

    @Test
    @DisplayName("getAll은 모든 종목을 SummaryDto로 변환해 반환한다")
    void getAll_returnsAllAsSummaryDtos() {
        when(repository.findAll()).thenReturn(List.of(
                buildExercise("Bench_Press", "Bench Press", "벤치 프레스"),
                buildExercise("Deadlift", "Deadlift", "데드리프트")
        ));
        when(favoriteRepository.findAllByIdUserId(1L)).thenReturn(List.of());

        List<ExerciseSummaryDto> result = service.getAll(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ExerciseSummaryDto::getId)
                .containsExactlyInAnyOrder("Bench_Press", "Deadlift");
    }

    @Test
    @DisplayName("getAll 결과에 한글명이 포함된다")
    void getAll_includesKoreanName() {
        when(repository.findAll()).thenReturn(List.of(
                buildExercise("Bench_Press", "Bench Press", "벤치 프레스")
        ));
        when(favoriteRepository.findAllByIdUserId(1L)).thenReturn(List.of());

        List<ExerciseSummaryDto> result = service.getAll(1L);

        assertThat(result.get(0).getKoreanName()).isEqualTo("벤치 프레스");
    }

    @Test
    @DisplayName("즐겨찾기 운동이 비즐겨찾기 운동보다 앞에 나온다")
    void getAll_withFavorites_favoritesComefirst() {
        ExerciseMaster fav = buildExercise("A", "Arm Curl", "암 컬");
        ExerciseMaster nonFav = buildExercise("B", "Back Squat", "백 스쿼트");
        when(repository.findAll()).thenReturn(List.of(nonFav, fav));
        when(favoriteRepository.findAllByIdUserId(1L)).thenReturn(List.of(buildFavorite(1L, "A")));

        List<ExerciseSummaryDto> result = service.getAll(1L);

        assertThat(result.get(0).getId()).isEqualTo("A");
        assertThat(result.get(1).getId()).isEqualTo("B");
    }

    @Test
    @DisplayName("즐겨찾기 운동의 isFavorite은 true다")
    void getAll_withFavorites_isFavoriteTrue() {
        when(repository.findAll()).thenReturn(List.of(buildExercise("A", "Arm Curl", "암 컬")));
        when(favoriteRepository.findAllByIdUserId(1L)).thenReturn(List.of(buildFavorite(1L, "A")));

        List<ExerciseSummaryDto> result = service.getAll(1L);

        assertThat(result.get(0).isFavorite()).isTrue();
    }

    @Test
    @DisplayName("즐겨찾기 없으면 모두 isFavorite=false다")
    void getAll_noFavorites_isFavoriteFalse() {
        when(repository.findAll()).thenReturn(List.of(
                buildExercise("A", "Arm Curl", "암 컬"),
                buildExercise("B", "Back Squat", "백 스쿼트")
        ));
        when(favoriteRepository.findAllByIdUserId(1L)).thenReturn(List.of());

        List<ExerciseSummaryDto> result = service.getAll(1L);

        assertThat(result).extracting(dto -> dto.isFavorite()).containsOnly(false);
    }

    @Test
    @DisplayName("같은 그룹(즐겨찾기/비즐겨찾기) 내에서는 가나다순을 유지한다")
    void getAll_sortedAlphabeticallyWithinSameGroup() {
        ExerciseMaster a = buildExercise("A", "A", "나");  // favorite
        ExerciseMaster b = buildExercise("B", "B", "차");  // favorite
        ExerciseMaster c = buildExercise("C", "C", "가");  // non-favorite
        ExerciseMaster d = buildExercise("D", "D", "마");  // non-favorite
        when(repository.findAll()).thenReturn(List.of(b, d, a, c));
        when(favoriteRepository.findAllByIdUserId(1L))
                .thenReturn(List.of(buildFavorite(1L, "A"), buildFavorite(1L, "B")));

        List<ExerciseSummaryDto> result = service.getAll(1L);

        assertThat(result).extracting(ExerciseSummaryDto::getKoreanName)
                .containsExactly("나", "차", "가", "마");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 getById 호출 시 CoreException이 발생한다")
    void getById_unknownId_throwsExerciseNotFound() {
        when(repository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("UNKNOWN"))
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("getById는 DetailDto를 반환하며 images 필드가 없다")
    void getById_existing_returnsDetailWithoutImages() {
        ExerciseMaster e = buildExercise("Bench_Press", "Bench Press", "벤치 프레스");
        e.setForce("push");
        e.setMechanic("compound");
        when(repository.findById("Bench_Press")).thenReturn(Optional.of(e));

        ExerciseDetailDto result = service.getById("Bench_Press");

        assertThat(result.getId()).isEqualTo("Bench_Press");
        assertThat(result.getName()).isEqualTo("Bench Press");
        assertThat(result.getKoreanName()).isEqualTo("벤치 프레스");
    }
}
