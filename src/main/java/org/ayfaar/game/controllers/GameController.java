package org.ayfaar.game.controllers;

import org.ayfaar.game.dao.CommonDao;
import org.ayfaar.game.model.*;
import org.ayfaar.game.utils.EntityUtils;
import org.ayfaar.game.utils.NextSituationGenerator;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Time;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.sort;
import static org.ayfaar.game.utils.ValueObjectUtils.getModelMap;

@Controller
@RequestMapping("api")
public class GameController {
    @Autowired CommonDao commonDao;
    @Autowired NextSituationGenerator nextSituationGenerator;
    private Random random = new Random();
    private static final int step = 250;

    @RequestMapping("start")
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

    @RequestMapping("edit-situation/{id}")
    @ResponseBody
    public Object getAddScreen(@PathVariable Integer id) {
        ModelMap map = new ModelMap();

        map.put("categories", getModelMap(commonDao.getAll(Category.class)));
        map.put("levels", getModelMap(commonDao.getAll(Level.class)));

        if (id > 0) {
            Situation situation = commonDao.get(Situation.class, id);
            sort(situation.getChoices(), new Comparator<Choice>() {
                @Override
                public int compare(Choice o1, Choice o2) {
                    return o1.getLevel().getId().compareTo(o2.getLevel().getId());
                }
            });
            map.put("situation", getModelMap(situation, "category", "choices.level"));
        }

        return map;
    }

    @RequestMapping(value = "save-situation", method = RequestMethod.POST)
    @ResponseBody
    public Integer saveSituation(@RequestParam("situation[text]") String situationText,
                             @RequestParam(value = "situation[id]", required = false) Integer situationId,
                             @RequestParam("situation[category][id]") Integer categoryId,
                             HttpServletRequest request) {
        HashMap<String, String[]> map = (HashMap<String, String[]>) request.getParameterMap();

        Situation situation;
        if (situationId != null) {
            situation = commonDao.get(Situation.class, situationId);
            Assert.notNull(situation);
        } else {
            situation = new Situation();
        }

        situation.setText(situationText);
        situation.setCategory(commonDao.get(Category.class, categoryId));

        ArrayList<Map.Entry<String, String[]>> entries = new ArrayList<Map.Entry<String, String[]>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, String[]>>() {
            @Override
            public int compare(Map.Entry<String, String[]> o1, Map.Entry<String, String[]> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        Map<Integer, Choice> choices = new HashMap<Integer, Choice>();
        for (Map.Entry<String, String[]> entry : entries) {
            Pattern pattern = Pattern.compile("situation\\[choices\\]\\[(\\d+)\\]\\[([^\\]]*)\\](.*)");
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.matches()) {
                Choice choice = choices.get(Integer.valueOf(matcher.group(1)));
                if (choice == null) {
                    choice = new Choice();
                    choice.setSituation(situation);
                    choices.put(Integer.valueOf(matcher.group(1)), choice);
                }
                if (matcher.group(2).equals("level")) {
                    if (matcher.group(3).equals("[id]"))
                        choice.setLevel(commonDao.get(Level.class, Integer.valueOf(entry.getValue()[0])));
                } else if (matcher.group(2).equals("levels")) {
                    // skip
                } else if (matcher.group(2).equals("id")) {
                    choice.setId(Integer.valueOf(entry.getValue()[0]));
                } else {
                    EntityUtils.setPropertyValue(choice, matcher.group(2), entry.getValue()[0]);
                }
            }
        }
        situation.setChoices(new ArrayList<Choice>(choices.values()));
        commonDao.save(situation);
        return situation.getId();
    }

    @RequestMapping("reset-user")
    @ResponseBody
    public void reset() {
        User user = commonDao.get(User.class, 1);
        for (UserLevel userLevel : user.getLevels()) {
            commonDao.remove(userLevel);
        }
        user.setLevels(Collections.EMPTY_LIST);
        user.setChoicesCounter(0);
        commonDao.save(user);
    }

}
