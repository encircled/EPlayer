$(document).ready(function () {

    // Localization
    $('span.ln').each(function (i, e) {
        var element = $(e);
        var html = element.html();
        var code = html.substring(3, html.length - 1);
        element.html(app.ln[code]);
    });

    app.callGetMediaTabs().forEach(function (tab) {
        model.tabs.push(tab);
        addTab(tab);
    });


    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var tabId = e.target.attributes['tab-id'].value;
        ui.selectedTabId = tabId;

        app.onTabUpdate(tabId);

        ui.searchInput.focus();
    });

    ui.tabsWrapper.find('a:first').tab('show');

    ui.searchInput.focus();
    ui.searchInput.keyup(function (e) {
        app.onFilterUpdate(e.target.value);
    });

    $('#resetFilter').click(function () {
        ui.searchInput.val('');
        app.onFilterUpdate('');
    });

    // View type
    $('#viewAll').click(function () {
        app.onViewTypeUpdate('ALL');
    });
    $('#viewSeries').click(function () {
        app.onViewTypeUpdate('SERIES');
    });
    $('#viewFilms').click(function () {
        app.onViewTypeUpdate('FILMS');
    });

    // Order by
    $('#orderByName').click(function () {
        app.onOrderByUpdate("NAME");
    });
    $('#orderBySize').click(function () {
        app.onOrderByUpdate("SIZE");
    });
    $('#orderByCreationDate').click(function () {
        app.onOrderByUpdate("CREATION_DATE");
    });

    // Reverse order
    $('#normalOrder').click(function () {
        app.onReverseOrderUpdate(false);
    });
    $('#reverseOrder').click(function () {
        app.onReverseOrderUpdate(true);
    });


});