package org.ayfaar.game.controllers;

import org.ayfaar.game.dao.CommonDao;
import org.ayfaar.game.model.*;
import org.ayfaar.game.utils.EntityUtils;
import org.ayfaar.game.utils.NextSituationGenerator;
import org.ayfaar.game.utils.ValueObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.sort;
import static org.ayfaar.game.utils.ValueObjectUtils.getModelMap;

@Controller
@RequestMapping("api/admin")
public class AdminController {
    @Autowired CommonDao commonDao;
    @Autowired NextSituationGenerator nextSituationGenerator;
    private Random random = new Random();
    private static final int step = 250;

    /*@RequestMapping("get/{id}")
    @ResponseBody
    public Object getSituation(@PathVariable Integer id) {
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
    }*/

    @RequestMapping("get-options")
    @ResponseBody
    public Object getOptions() {
        ModelMap map = new ModelMap();

        map.put("categories", getModelMap(commonDao.getAll(Category.class)));
        map.put("levels", getModelMap(commonDao.getAll(Level.class)));

        return map;
    }

    @RequestMapping(value = "save-situation", method = RequestMethod.POST)
    @ResponseBody
    public Integer saveSituation(@RequestParam("text") String situationText,
                                 @RequestParam(value = "id", required = false) Integer situationId,
                                 @RequestParam("category[id]") Integer categoryId,
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
        sort(entries, new Comparator<Map.Entry<String, String[]>>() {
            @Override
            public int compare(Map.Entry<String, String[]> o1, Map.Entry<String, String[]> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        Map<Integer, Choice> choices = new HashMap<Integer, Choice>();
        for (Map.Entry<String, String[]> entry : entries) {
            Pattern pattern = Pattern.compile("choices\\[(\\d+)\\]\\[([^\\]]*)\\](.*)");
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

    @RequestMapping("all-situations")
    @ResponseBody
    public Object getAllSituations() throws Exception {
        List<Situation> all = commonDao.getAll(Situation.class);

        return ValueObjectUtils.convertToPlainObjects(all, new ValueObjectUtils.Modifier<Situation>() {
            @Override
            public void modify(Situation entity, ModelMap map) throws Exception {
                sort(entity.getChoices(), new Comparator<Choice>() {
                    @Override
                    public int compare(Choice o1, Choice o2) {
                        return o1.getLevel().getId().compareTo(o2.getLevel().getId());
                    }
                });
                map.put("choices", getModelMap(entity.getChoices(), "level"));
                map.put("category", getModelMap(entity.getCategory()));
            }
        });
    }
}
