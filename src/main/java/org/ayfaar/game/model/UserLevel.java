package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class UserLevel implements Comparable<UserLevel> {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer value;

    @ManyToOne
    private User user;
    @ManyToOne
    private Level level;

    @Override
    public int compareTo(UserLevel o) {
        return level.getId().compareTo(o.getLevel().getId());
    }
}
