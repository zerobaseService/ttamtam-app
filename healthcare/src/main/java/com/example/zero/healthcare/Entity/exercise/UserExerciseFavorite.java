package com.example.zero.healthcare.Entity.exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_exercise_favorite")
@Getter
@Setter
@NoArgsConstructor
public class UserExerciseFavorite {

    @EmbeddedId
    private UserExerciseFavoriteId id;

    @MapsId("exerciseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private ExerciseMaster exercise;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
