package org.ayfaar.game.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "id")
public class Level implements Comparable<Level> {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;

    public Level(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Level o) {
        return id.compareTo(o.getId());
    }
}
