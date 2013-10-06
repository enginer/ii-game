package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Choice {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(columnDefinition = "TEXT")
    private String text;
    @Column(columnDefinition = "TEXT")
    private String resume;

    @ManyToOne
    private Level level;

    @ManyToOne
    private Situation situation;
}
