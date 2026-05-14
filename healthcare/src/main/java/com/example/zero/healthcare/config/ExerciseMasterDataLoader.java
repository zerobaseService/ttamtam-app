package com.example.zero.healthcare.config;

import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseMasterDataLoader implements ApplicationRunner {

    private final ExerciseMasterRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.exercise-data.path:exercise-data/exercises.json}")
    private String exercisesPath;

    @Value("${app.exercise-data.korean-names-path:exercise-data/exercise-korean-names.json}")
    private String koreanNamesPath;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(exercisesPath);
        List<ExerciseJson> rawList;
        try (InputStream is = resource.getInputStream()) {
            rawList = objectMapper.readValue(is, new TypeReference<>() {});
        }

        List<ExerciseMaster> exercises = rawList.stream()
                .map(ExerciseJson::toEntity)
                .toList();

        Map<String, String> koreanMap = loadKoreanNames();
        int mappedCount = 0;
        for (ExerciseMaster e : exercises) {
            String korean = koreanMap.get(e.getId());
            if (korean != null) {
                e.updateKoreanName(korean);
                mappedCount++;
            }
        }

        repository.saveAll(exercises);
        log.info("Loaded {} exercises, applied {} Korean name mappings", exercises.size(), mappedCount);
    }

    private Map<String, String> loadKoreanNames() {
        ClassPathResource resource = new ClassPathResource(koreanNamesPath);
        if (!resource.exists()) {
            log.warn("Korean names file not found: {}. Skipping Korean name mapping.", koreanNamesPath);
            return Map.of();
        }
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to load Korean names file: {}. Skipping.", koreanNamesPath, e);
            return Map.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExerciseJson(
            String id,
            String name,
            String force,
            String level,
            String mechanic,
            String equipment,
            String category,
            List<String> primaryMuscles,
            List<String> secondaryMuscles
    ) {
        ExerciseMaster toEntity() {
            ExerciseMaster e = new ExerciseMaster();
            e.setId(id);
            e.setName(name);
            e.setForce(force);
            e.setLevel(level);
            e.setMechanic(mechanic);
            e.setEquipment(equipment);
            e.setCategory(category);
            e.setPrimaryMuscles(primaryMuscles != null ? new HashSet<>(primaryMuscles) : Set.of());
            e.setSecondaryMuscles(secondaryMuscles != null ? new HashSet<>(secondaryMuscles) : Set.of());
            return e;
        }
    }
}
