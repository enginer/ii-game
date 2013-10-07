var game = {};
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

    function nextSituation() {
        $.get("api/game/next", function(r) {
            situationId = r.situation.id;
            r.editLink = "#admin";//+ r.situation.id;
            r.reset = function(e) {
                $.get("api/admin/reset-user", nextSituation)
            };
            var viewModel = kendo.observable(r);
            kendo.bind($('#game'), viewModel);
        })
    }

    game.choose = function(choiceId) {
        $.get("api/game/situation/"+ situationId +"/choice/"+ choiceId, nextSituation)
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
        }
    }).listen('hash');
});