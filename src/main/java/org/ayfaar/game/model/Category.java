package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Time;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private Time fromTime;
    private Time toTime;
    private Boolean restDay;
}
