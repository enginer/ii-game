package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Time;
import java.util.List;

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

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<UserLevel> levels;
}
