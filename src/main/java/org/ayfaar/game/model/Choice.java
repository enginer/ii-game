package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Choice implements Comparable<Choice> {
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

    @Override
    public int compareTo(Choice o) {
        return getLevel().getId().compareTo(o.getLevel().getId());
    }
}
