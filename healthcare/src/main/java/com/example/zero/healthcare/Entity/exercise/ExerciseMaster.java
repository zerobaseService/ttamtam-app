package com.example.zero.healthcare.Entity.exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exercise_master")
@Getter
@Setter
@NoArgsConstructor
public class ExerciseMaster {

    @Id
    @Column(length = 100)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "korean_name", length = 200)
    private String koreanName;

    @Column(name = "exercise_force", length = 20)
    private String force;

    @Column(length = 20)
    private String level;

    @Column(length = 20)
    private String mechanic;

    @Column(length = 50)
    private String equipment;

    @Column(length = 50)
    private String category;

    @Fetch(FetchMode.SUBSELECT)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_master_primary_muscles", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle")
    private Set<String> primaryMuscles = new HashSet<>();

    @Fetch(FetchMode.SUBSELECT)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_master_secondary_muscles", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle")
    private Set<String> secondaryMuscles = new HashSet<>();

    public void updateKoreanName(String koreanName) {
        this.koreanName = koreanName;
    }
}
