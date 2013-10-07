(function(game) {
    var categories, levels;

    game.init = function() {
        loadOptions()
        loadSituations();
    }

    function loadOptions() {
        $.get("api/admin/get-options", function(r) {
            categories = new kendo.data.ObservableArray(r.categories);
            levels = new kendo.data.ObservableArray(r.levels);
        })
    }
    function loadSituations() {
        $("#grid").kendoGrid({
            dataSource: {
                transport: {
                    read: "api/admin/all-situations"
                },
                group: {
                    field: "category.name",
                    aggregates: [{ field: "text", aggregate: "count"}]
                }
            },
            selectable: true,
            columns: [
                { field: "text", title: "Ситуация"},
                { field: "category.name", title: "Категория", hidden: true,
                    groupHeaderTemplate: "<strong>#= value #</strong>"}
            ],
            height: 500,
            change: function(e) {
                var selectedRows = this.select();
                var selectedDataItems = [];
                for (var i = 0; i < selectedRows.length; i++) {
                    var dataItem = this.dataItem(selectedRows[i]);
                    selectedDataItems.push(dataItem);
                }
                showSituation(selectedDataItems[0])
            },
            toolbar: [{ template: '<a class="k-button" href="\\#" onclick="return game.newSituation()">Новая ситуация</a>'}]
        });
    };

    game.newSituation = function(e) {
        var grid = $("#grid").data("kendoGrid");
        var item = grid.dataSource.add({text: "New...", category: categories[0], choices: []});
        showSituation(item);
        return false;
    }
    function showSituation(r) {
        if (!r.choices) {
            r.choices = [];
        }
        r.getCategories = function(){return categories};
        r.setLevels = function(){return levels};
        r.save = function(e) {
            /*if (!e.data.category) {
                alert("Выберите категорию");
                return;
            }*/
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
            $.post("api/admin/save-situation", o, function(id){
                noty({text: "Сохранено", type: 'alert', layout: 'topCenter', timeout: 1000});
                var grid = $("#grid").data("kendoGrid");
                grid.dataSource.read();
//                loadAddData(id)
            })
        };

        r.removeChoice = function(e) {
            var index = e.data.parent().indexOf(e.data);
            if (index > -1) {
                e.data.parent().splice(index, 1);
            }
        }
        r.addChoice = function(e) {
            e.data.choices.unshift(kendo.observable({levels: levels, text: "", resume:""}));
        };
//        var viewModel = kendo.observable(r);
        kendo.bind($('#situation'), r);
    }
})(window.game ? window.game : {});
