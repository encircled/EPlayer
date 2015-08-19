var ui = {

    selectedTabId: null,

    tabsWrapper: $('#tabs-wrapper'),

    tabsContentWrapper: $('#tabs-content-wrapper'),

    searchInput: $('#searchInput'),

    getTabById: function (tabId) {
        return $('#tab_' + tabId);
    }

};