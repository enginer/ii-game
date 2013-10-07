package org.ayfaar.game.controllers;

import org.ayfaar.game.dao.CommonDao;
import org.ayfaar.game.model.*;
import org.ayfaar.game.utils.NextSituationGenerator;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Time;
import java.util.*;

import static java.util.Collections.sort;
import static org.ayfaar.game.utils.ValueObjectUtils.getModelMap;

@Controller
@RequestMapping("api/game")
public class GameController {
    @Autowired CommonDao commonDao;
    @Autowired NextSituationGenerator nextSituationGenerator;
    private Random random = new Random();
    private static final int step = 250;

    @RequestMapping("next")
    @ResponseBody
    public Object next() {
        ModelMap map = new ModelMap();

        User user = commonDao.get(User.class, 1);

        if (user.getTime() == null) {
            newDay(user);
        }

        DateTime dateTime = new DateTime(user.getTime());
        dateTime = dateTime.plusMinutes(10 + random.nextInt(step));

        if (dateTime.getHourOfDay() < 8) {
            newDay(user);
            dateTime = new DateTime(user.getTime());
        } else {
            user.setTime(new Time(dateTime.toDate().getTime()));
        }

        sort(user.getLevels());
        commonDao.save(user);
        map.put("user", getModelMap(user, "levels.level"));

        Situation situation = null;
        Set<Choice> choices = null;

        while (choices == null || choices.size() < 1) {
            situation = nextSituationGenerator.getNext(user.getTime(), user.getRestDay());

            UserLevel maxUserLevel = null;
            for (UserLevel userLevel : user.getLevels()) {
                if (maxUserLevel == null || userLevel.getValue().compareTo(maxUserLevel.getValue()) == 1) {
                    maxUserLevel = userLevel;
                }
            }
            if (maxUserLevel == null) {
                maxUserLevel = new UserLevel();
                maxUserLevel.setLevel(new Level(10));
            }

            choices = new HashSet<Choice>();

            for (Choice choice : situation.getChoices()) {
                Integer primaryUserLevelId = maxUserLevel.getLevel().getId();
                Integer choiceLevelId = choice.getLevel().getId();
                if (choiceLevelId.equals(primaryUserLevelId)
                        || choiceLevelId.equals(primaryUserLevelId - 1)
                        || choiceLevelId.equals(primaryUserLevelId + 1)
                        ) {
                    choices.add(choice);
                }
            }
        }

        ModelMap situationModelMap = (ModelMap) getModelMap(situation, "category");
        situationModelMap.put("choices", getModelMap(choices, "level"));

        map.put("situation", situationModelMap);


        return map;
    }

    private void newDay(User user) {
        user.setTime(new Time(9, random.nextInt(step), 0));
        user.setRestDay(random.nextDouble() <= (double)3/7);
    }

    @RequestMapping("situation/{situationId}/choice/{choiceId}")
    @ResponseBody
    public void choice(@PathVariable Integer situationId,
                         @PathVariable Integer choiceId) {
        Choice choice = commonDao.get(Choice.class, choiceId);
        User user = commonDao.get(User.class, 1);
        UserLevel currentLevel = null;
        for (UserLevel userLevel : user.getLevels()) {
            if (userLevel.getLevel().equals(choice.getLevel())) {
                currentLevel = userLevel;
            }
        }
        if (currentLevel == null) {
            currentLevel = new UserLevel();
            currentLevel.setUser(user);
            currentLevel.setLevel(choice.getLevel());
            currentLevel.setValue(0);
        }

        currentLevel.setValue(currentLevel.getValue() + 1);
        commonDao.save(currentLevel);
        if (user.getChoicesCounter() != null) {
            user.setChoicesCounter(user.getChoicesCounter() + 1);
        } else {
            user.setChoicesCounter(1);
        }
        commonDao.save(user);
        ChoiceLog log = new ChoiceLog();
        log.setTime(new Date());
        log.setUser(user);
        log.setChoice(choice);
        commonDao.save(log);

        for (ChoiceLog choiceLog : commonDao.getFor(ChoiceLog.class, "user", user.getId())) {
            commonDao.remove(choiceLog);
        }
    }

}
