package cz.encircled.eplayer.util;

public enum Localization {

    file,

    language,

    save,

    cancel,

    settings,

    tools,

    open,

    exit,

    openQuickNavi,

    audioPassThrough,

    errorTitle,

    confirmTitle,

    msgQnFileCorrupted,

    msgQnFileIoFail,

    media,

    fileOpenFailed,

    subtitles,

    audioTrack,

    view,

    fullScreen,

    play,

    pause,

    fitScreen,

    deleteMissing,

    today,

    yesterday,

    daysAgo,

    back,

    all,

    films,

    aggregationType,

    sortType,

    series,

    name,

    size,

    search,

    creationDate;

    public String ln() {
        return LocalizationProvider.get(this.name());
    }

    public String ln(String param) {
        return LocalizationProvider.get(this.name() + "." + param);
    }

}
