package com.example.zero.healthcare.Entity.journal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_exercise")
@Getter
@NoArgsConstructor
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private WorkoutJournal journal;

    @Column(name = "exercise_name", nullable = false, length = 100)
    private String exerciseName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @OneToMany(mappedBy = "exercise", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    @OrderBy("setNumber ASC")
    private List<WorkoutSet> sets = new ArrayList<>();

    @Builder
    public WorkoutExercise(String exerciseName, Integer displayOrder) {
        this.exerciseName = exerciseName;
        this.displayOrder = displayOrder;
    }

    public void addSet(WorkoutSet set) {
        sets.add(set);
        set.setExercise(this);
    }

    void setJournal(WorkoutJournal journal) {
        this.journal = journal;
    }
}
