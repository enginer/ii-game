package org.ayfaar.game.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Data
public class ChoiceLog {
    @Id
    @GeneratedValue
    private Integer id;
    private Date time;
    @ManyToOne
    private User user;
    @ManyToOne
    private Choice choice;
}
