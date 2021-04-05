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

    series,

    name,

    size,

    search,

    creationDate;

    public String ln() {
        return LocalizationProvider.get2(this.name());
    }
}
