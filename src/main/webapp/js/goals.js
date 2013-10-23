(function(game) {
    var categories, levels;

    game.goalsInit = function() {
        loadOptions()
        loadGoals();
    }

    function loadOptions() {
        $.get("api/admin/get-options", function(r) {
            categories = new kendo.data.ObservableArray(r.categories);
            levels = new kendo.data.ObservableArray(r.levels);
        })
    }
    function loadGoals() {
        $("#goals-grid").kendoGrid({
            dataSource: {
                transport: {
                    read: "api/admin/goal/all"
                }
            },
            selectable: true,
            columns: [
                { field: "name", title: "Цель"}
            ],
            height: 500,
            change: function(e) {
                var selectedRows = this.select();
                var selectedDataItems = [];
                for (var i = 0; i < selectedRows.length; i++) {
                    var dataItem = this.dataItem(selectedRows[i]);
                    selectedDataItems.push(dataItem);
                }
                showGoal(selectedDataItems[0])
            },
            toolbar: [{ template: '<a class="k-button" href="\\#" onclick="return game.newGoal()">Новая цель</a>'}]
        });
    };

    game.newGoal = function(e) {
        var grid = $("#goals-grid").data("kendoGrid");
        var item = grid.dataSource.add({name: "Новая цель", stages: []});
        showGoal(item);
        return false;
    }
    function showGoal(r) {
        if (!r.stages) {
            r.stages = [];
        }
        r.getMaxChoices = function(data){
            return data.maxChoices == "true"
        }
        r.getCategories = function(){return categories};
        r.setLevels = function(){return levels};
        r.save = function(e) {
            var o = e.data.toJSON();
            $.post("api/admin/goal/save", o, function(id){
                noty({text: "Сохранено", type: 'alert', layout: 'topCenter', timeout: 1000});
                var grid = $("#goals-grid").data("kendoGrid");
                grid.dataSource.read();
            })
        };

        r.removeStage = function(e) {
            var index = e.data.parent().indexOf(e.data);
            if (index > -1) {
                e.data.parent().splice(index, 1);
            }
            kendo.bind($('#goal'), e.data);
        }
        r.addStage = function(e) {
            e.data.stages.unshift(kendo.observable({levels: levels, text: "", from: false}));
            kendo.bind($('#goal'), e.data);
        };
        kendo.bind($('#goal'), r);
    }
})(window.game ? window.game : {});
