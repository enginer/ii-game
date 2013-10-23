package org.ayfaar.game.model;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
@Data
public class Goal {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    private Level startLevel;
    @ManyToOne
    private Level finishLevel;
    private Integer maxChoices;
    private String name;
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "goal", cascade = ALL)
    private List<Stage> stages;
}
