package com.example.zero.healthcare.exercise.config;

import com.example.zero.healthcare.config.ExerciseMasterDataLoader;
import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseMasterDataLoaderTest {

    @Mock
    private ExerciseMasterRepository repository;

    @InjectMocks
    private ExerciseMasterDataLoader loader;

    @Captor
    private ArgumentCaptor<List<ExerciseMaster>> savedCaptor;

    private void injectPaths(String exercisePath, String koreanPath) {
        ReflectionTestUtils.setField(loader, "exercisesPath", exercisePath);
        ReflectionTestUtils.setField(loader, "koreanNamesPath", koreanPath);
    }

    @Test
    @DisplayName("빈 테이블에 픽스처 파일로 로딩하면 운동 종목이 저장된다")
    void loader_emptyTable_loadsAllExercises() throws Exception {
        injectPaths(
                "exercise-data/exercises-fixture.json",
                "exercise-data/exercise-korean-names-fixture.json"
        );
        when(repository.count()).thenReturn(0L);

        loader.run(null);

        verify(repository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(3);
    }

    @Test
    @DisplayName("테이블이 비어있지 않으면 로딩을 건너뛴다")
    void loader_nonEmptyTable_skipsLoading() throws Exception {
        injectPaths(
                "exercise-data/exercises-fixture.json",
                "exercise-data/exercise-korean-names-fixture.json"
        );
        when(repository.count()).thenReturn(10L);

        loader.run(null);

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("픽스처 로딩 시 한글 매핑이 적용된다")
    void loader_appliesKoreanMapping() throws Exception {
        injectPaths(
                "exercise-data/exercises-fixture.json",
                "exercise-data/exercise-korean-names-fixture.json"
        );
        when(repository.count()).thenReturn(0L);

        loader.run(null);

        verify(repository).saveAll(savedCaptor.capture());
        List<ExerciseMaster> saved = savedCaptor.getValue();
        ExerciseMaster bench = saved.stream()
                .filter(e -> "Bench_Press".equals(e.getId()))
                .findFirst().orElseThrow();
        assertThat(bench.getKoreanName()).isEqualTo("벤치 프레스");
    }

    @Test
    @DisplayName("한글 매핑 파일이 없어도 예외 없이 1단계만 수행한다")
    void loader_missingKoreanFile_skipsKoreanStepWithoutError() throws Exception {
        injectPaths(
                "exercise-data/exercises-fixture.json",
                "exercise-data/nonexistent-korean-file.json"
        );
        when(repository.count()).thenReturn(0L);

        assertThatNoException().isThrownBy(() -> loader.run(null));
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("JSON의 images와 instructions 필드는 무시된다 (ExerciseMaster에 해당 필드 없음)")
    void loader_ignoresImagesAndInstructionsFromJson() throws Exception {
        injectPaths(
                "exercise-data/exercises-fixture.json",
                "exercise-data/exercise-korean-names-fixture.json"
        );
        when(repository.count()).thenReturn(0L);

        loader.run(null);

        verify(repository).saveAll(savedCaptor.capture());
        List<ExerciseMaster> saved = savedCaptor.getValue();
        assertThat(saved).isNotEmpty();
    }
}
