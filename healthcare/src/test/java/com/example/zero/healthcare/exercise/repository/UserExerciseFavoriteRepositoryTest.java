package com.example.zero.healthcare.exercise.repository;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.Entity.exercise.ExerciseMaster;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavorite;
import com.example.zero.healthcare.Entity.exercise.UserExerciseFavoriteId;
import com.example.zero.healthcare.repository.ExerciseMasterRepository;
import com.example.zero.healthcare.repository.UserExerciseFavoriteRepository;
import com.example.zero.healthcare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserExerciseFavoriteRepositoryTest {

    @Autowired
    private UserExerciseFavoriteRepository favoriteRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ExerciseMasterRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    private Long userId;
    private ExerciseMaster exercise;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User(UUID.randomUUID() + "@test.com", "token", "tester"));
        userId = user.getId();

        ExerciseMaster e = new ExerciseMaster();
        e.setId("FavTest_" + UUID.randomUUID().toString().substring(0, 8));
        e.setName("Favorite Test Exercise");
        e.setLevel("beginner");
        e.setCategory("strength");
        e.setEquipment("barbell");
        exercise = exerciseRepository.save(e);
    }

    private UserExerciseFavorite buildFavorite(Long uId, ExerciseMaster ex) {
        UserExerciseFavoriteId pk = new UserExerciseFavoriteId(uId, ex.getId());
        UserExerciseFavorite fav = new UserExerciseFavorite();
        fav.setId(pk);
        fav.setExercise(ex);
        fav.setCreatedAt(LocalDateTime.now());
        return fav;
    }

    @Test
    @DisplayName("새로운 즐겨찾기를 저장하면 영속된다")
    void save_newFavorite_persists() {
        UserExerciseFavorite fav = buildFavorite(userId, exercise);
        UserExerciseFavorite saved = favoriteRepository.save(fav);

        assertThat(favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, exercise.getId())).isTrue();
        assertThat(saved.getId().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("동일 (userId, exerciseId)로 중복 저장 시 예외가 발생한다")
    void save_duplicateUserAndExercise_throwsConstraintViolation() {
        favoriteRepository.save(buildFavorite(userId, exercise));
        favoriteRepository.flush();
        em.clear();

        UserExerciseFavorite dup = buildFavorite(userId, exercise);
        assertThatThrownBy(() -> {
            em.persist(dup);
            em.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("existsBy 메서드로 존재 여부를 정확히 확인한다")
    void existsByIdUserIdAndIdExerciseId_existing_returnsTrue() {
        favoriteRepository.save(buildFavorite(userId, exercise));

        assertThat(favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, exercise.getId())).isTrue();
        assertThat(favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, "NONEXISTENT")).isFalse();
    }

    @Test
    @DisplayName("deleteBy 메서드로 즐겨찾기를 삭제한다")
    void deleteByIdUserIdAndIdExerciseId_existing_removes() {
        favoriteRepository.save(buildFavorite(userId, exercise));
        favoriteRepository.flush();

        favoriteRepository.deleteByIdUserIdAndIdExerciseId(userId, exercise.getId());
        favoriteRepository.flush();

        assertThat(favoriteRepository.existsByIdUserIdAndIdExerciseId(userId, exercise.getId())).isFalse();
    }

    @Test
    @DisplayName("findAllByIdUserId는 해당 사용자의 즐겨찾기만 반환한다")
    void findAllByIdUserId_returnsOnlyOwnFavorites() {
        User other = userRepository.save(new User(UUID.randomUUID() + "@other.com", "t", "other"));

        ExerciseMaster e2 = new ExerciseMaster();
        e2.setId("FavTest2_" + UUID.randomUUID().toString().substring(0, 8));
        e2.setName("Another Exercise");
        e2.setLevel("beginner");
        e2.setCategory("strength");
        e2.setEquipment("barbell");
        ExerciseMaster saved2 = exerciseRepository.save(e2);

        favoriteRepository.save(buildFavorite(userId, exercise));
        favoriteRepository.save(buildFavorite(other.getId(), saved2));

        List<UserExerciseFavorite> result = favoriteRepository.findAllByIdUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId().getUserId()).isEqualTo(userId);
    }
}
