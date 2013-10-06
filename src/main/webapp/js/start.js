var game = {};
$(document).ready(function() {
    var situationId;

    function loadAddData(id) {
        $.get("api/edit-situation/"+(id?id:0), function(r) {
//            r.choices = [];
            if (!r.situation) {
                r.situation = {choices: []};
            }
            r.save = function(e) {
                if (!e.data.situation.category) {
                    alert("Выберите категорию");
                    return;
                }
                /*var choices = [];
                for(var i=0; i< e.data.choices.length;i++   ) {
                    var choice = e.data.choices[i];
                    choices.push({
                        level: choice.level.id,
                        text: choice.text,
                        resume: choice.resume
                    })
                }
                var o = {
                    category: e.data.category.id,
                    situation: e.data.situation,
                    choices: choices
                };*/
                var o = e.data.toJSON();
                delete o.categories;
                delete o.levels;
                $.post("api/save-situation", o, function(id){
                    alert("Сохранено");
                    loadAddData(id)
                })
            };

            r.addChoice = function(e) {
                e.data.situation.choices.push(kendo.observable({levels: e.data.levels, text: "", resume:""}));
            };
            var viewModel = kendo.observable(r);
            kendo.bind($('#add'), viewModel);
        })
    }
    function nextSituation() {
        $.get("api/start", function(r) {
            situationId = r.situation.id;
            r.editLink = "#edit/"+ r.situation.id;
            r.reset = function(e) {
                $.get("api/reset-user", nextSituation)
            };
            var viewModel = kendo.observable(r);
            kendo.bind($('#game'), viewModel);
        })
    }

    game.choose = function(choiceId) {
        $.get("api/situation/"+ situationId +"/choice/"+ choiceId, nextSituation)
    };

    $.history.on('load change push', function(event, hash, type) {
        if (hash.indexOf("edit/")==0) {
            ensure({ html: "edit.html", parent: "content"}, function(){
                loadAddData(hash.replace("edit/", ""));
            });
            return
        }
        switch (hash) {
            case "":
            case "play":
                ensure({ html: "game.html", parent: "content"}, function(){
                    nextSituation();
                });
                break;
            case "add":
                ensure({ html: "edit.html", parent: "content"}, function(){
                    loadAddData(0);
                });
                break;
        }
    }).listen('hash');
});