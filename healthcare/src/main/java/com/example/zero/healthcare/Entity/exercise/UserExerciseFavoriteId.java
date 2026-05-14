package com.example.zero.healthcare.Entity.exercise;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserExerciseFavoriteId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "exercise_id", length = 100)
    private String exerciseId;
}
