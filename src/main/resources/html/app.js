app = {

    callGetMediaTabs: function () {
        console.log('Load tabs');
        return JSON.parse(bridge.getMediaTabs());
    },

    callPlayMedia: function (path) {
        console.log('Call play media at path: ' + path);
        bridge.playMedia(path);
    },

    callCloseTab: function (path) {
        console.log('Call close tab: ' + path);
        bridge.closeTab(path);
    },

    onFilterUpdate: function (filter) {
        bridge.onFilterUpdate(filter);
    },

    onTabUpdate: function (tabId) {
        var tabPath = model.getTabById(tabId).path;
        bridge.onTabUpdate(tabPath);
    },

    onViewTypeUpdate: function (viewType) {
        bridge.onViewTypeUpdate(viewType);
    },

    onOrderByUpdate: function (orderBy) {
        bridge.onOrderByUpdate(orderBy);
    },

    onReverseOrderUpdate: function (isReverseOrder) {
        bridge.onReverseOrderUpdate(isReverseOrder);
    },

    ln: JSON.parse(bridge.getLocalization())

};

function showMediaCallback(arg) {
    var params = JSON.parse(arg);
    var path = params[0];
    var media = params[1];

    console.log('showMediaCallback: tab path is {0}'.format(path));

    var tabId = model.getTabByPath(path).id;
    var tab = ui.getTabById(tabId);

    tab.html('');
    media.forEach(function (m) {
        var mediaWrapper = components.getMediaWrapper(m);
        mediaWrapper.appendTo(tab);
        mediaWrapper.click(function () {
            app.callPlayMedia(m.path);
        });
        mediaWrapper.find('.glyphicon-info-sign').tooltip({});
    });

    setTimeout("refreshImg()", 1);
}

function refreshImg(tabId) {
    var time = new Date().getTime();
    $('img.media-screenshot').each(function (index, img) {
        img.src = img.src + '?' + time;
    });
}

function addTab(tab) {
    var tabStick = components.getTab(tab);
    var tabContent = components.getTabContent(tab);

    tabContent.appendTo(ui.tabsContentWrapper);
    tabStick.appendTo(ui.tabsWrapper);

    model.tabs.push(tab);

    tabStick.find('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var tabId = e.target.attributes['tab-id'].value;
        ui.selectedTabId = tabId;

        app.onTabUpdate(tabId);

        ui.searchInput.focus();
    });

    if (tab.closeable) {
        tabStick.find('.tab-close-button').click(function () {
            tabStick.remove();
            tabContent.remove();
            app.callCloseTab(tab.path);
        });
    }
}

function addTabCallback(arg) {
    var tab = JSON.parse(arg);
    addTab(tab);
}
