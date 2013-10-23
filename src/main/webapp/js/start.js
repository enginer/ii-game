var game = {
    error: function (text) {
        noty({text: text, type: 'error', layout: 'topCenter', timeout: 1000});
    }
};

$.ajaxSetup({
    error: function (e, status, error) {
        if (e.status == 400) {
            game.error("Ошибка запроса, указаны не все данные");
        }
        else if (e.status == 404) {
            game.error("URL not found: "+this.url);
        }
        else {
            error = e.responseText.indexOf("<") == 0
                ? e.responseText
                : JSON.parse(e.responseText);
            if (error && error.error) {
                if (error.error.code != "UNDEFINED") {
                    game.error(sf.labels.getLabel(error.error.code))
                } else {
                    game.error(error.error.message)
                }
            } else {
                game.error(e.responseText)
            }
        }
    }
});

$(document).ready(function() {
    var situationId;

    if (!location.hash) {
        loadGame();
    }

    function loadGame() {
        ensure({ html: "game.html", parent: "content"}, function(){
            nextSituation();
        });
    }
    function loadAdmin(situationId) {
        ensure({ html: "admin.html", js: "js/admin.js", parent: "content"}, function(){
            game.init();
        });
    }
    function loadAdminGoals() {
        ensure({ html: "goals.html", js: "js/goals.js", parent: "content"}, function(){
            game.goalsInit();
        });
    }

    function nextSituation() {
        $.get("api/game/next", function(r) {
            if (r.message) {
                alert(r.message);
                return;
            }
            situationId = r.situation ? r.situation.id : 0;
            r.editLink = "#admin";//+ r.situation.id;
            r.reset = function(e) {
                $.get("api/admin/reset-user", nextSituation)
            };
            r.onGoalChanged = function(e) {
                $.get("api/game/change-goal", {
                    userId: e.data.user.id, goalId: e.data.user.currentGoal.id}, function(){
                    noty({text: "Изменено", type: 'alert', layout: 'topCenter', timeout: 1000});
                })
            }
            var viewModel = kendo.observable(r);
            kendo.bind($('#game'), viewModel);
        })
    }

    game.choose = function(choiceId) {
        $.get("api/game/situation/"+ situationId +"/choice/"+ choiceId, function(r) {
            if (r) {
                alert(r);
            }
            nextSituation();
        })
    };

    $.history.on('load change push', function(event, hash, type) {
        if (hash.indexOf("edit/")==0) {
            loadAdmin(hash.replace("edit/", ""));
            return
        }
        switch (hash) {
            case "play":
                loadGame();
                break;
            case "admin":
                loadAdmin(0);
                break;
            case "admin-goals":
                loadAdminGoals();
                break;
        }
    }).listen('hash');
});