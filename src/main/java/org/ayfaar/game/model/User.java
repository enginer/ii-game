package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Time;
import java.util.List;

import static javax.persistence.FetchType.EAGER;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue
    private Integer id;
    private Time time;
    private Boolean restDay;
    private Integer availableChoices;
    private Integer choicesCounter;
    @ManyToOne
    private Goal currentGoal;

    @OneToMany(mappedBy = "user", fetch = EAGER)
    private List<UserLevel> levels;
}
