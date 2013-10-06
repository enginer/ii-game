package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Situation {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(columnDefinition = "TEXT")
    private String text;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "situation")
    private List<Choice> choices;

    @ManyToOne
    private Category category;
}
