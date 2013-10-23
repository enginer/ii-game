package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Stage {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    private Level level;
    @ManyToOne
    private Goal goal;
    @Column(name = "_from")
    private Boolean from;
    private String text;
}
