package org.ayfaar.game.utils;

import org.ayfaar.game.dao.CommonDao;
import org.ayfaar.game.model.Category;
import org.ayfaar.game.model.Situation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class NextSituationGenerator {
    @Autowired CommonDao commonDao;
    private Random randomGenerator = new Random();

    public Situation getNext(Time time, Boolean restDay) {
        List<Category> categories = commonDao.getAll(Category.class);
        List<Situation> situations = new ArrayList<Situation>();

        for (Category category : categories) {
            if (((category.getFromTime().before(time) && category.getToTime().after(time))
                || category.getFromTime().equals(time) || category.getToTime().equals(time))
                    && (category.getRestDay() == null || category.getRestDay().equals(restDay))) {
                situations.addAll(commonDao.getFor(Situation.class, "category", category.getId()));
            }
        }

        return situations.get(randomGenerator.nextInt(situations.size()));
    }
}
