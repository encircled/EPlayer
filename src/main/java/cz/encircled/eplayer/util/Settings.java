package cz.encircled.eplayer.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * @author Encircled on 04-Dec-14.
 */
public enum Settings {

    language,

    fc_open_location,

    max_volume,

    last_volume,

    folders_to_scan;

    public String get(@NotNull String defaultValue) {
        return SettingsProvider.get(this.name(), defaultValue);
    }

    @NotNull
    public List<String> getList() {
        return SettingsProvider.getList(this.name());
    }

    public String get() {
        return SettingsProvider.get(this.name());
    }

    @Nullable
    public Integer getInt() {
        return SettingsProvider.getInt(this.name());
    }

    public Integer getInt(int defaultValue) {
        return SettingsProvider.getInt(this.name(), defaultValue);
    }

    @NotNull
    public Settings set(@Nullable Object value) {
        SettingsProvider.set(this.name(), value);
        return this;
    }

    @NotNull
    public Settings addToList(@Nullable String value) {
        SettingsProvider.addToList(this.name(), value);
        return this;
    }

    @NotNull
    public Settings removeFromList(@Nullable String value) {
        SettingsProvider.removeFromList(this.name(), value);
        return this;
    }

    public void save() {
        SettingsProvider.save();
    }

}
