package dev.mg.wannacry.event.impl;

import dev.mg.wannacry.event.Event;
import dev.mg.wannacry.features.Feature;
import dev.mg.wannacry.features.settings.Setting;

public class ClientEvent extends Event {
    private final Type type;
    private final Feature feature;
    private Setting<?> setting;

    public ClientEvent(Type type, Feature feature) {
        this.type = type;
        this.feature = feature;
    }

    public ClientEvent(Setting<?> setting) {
        this(Type.SETTING_UPDATE, setting.getFeature());
        this.setting = setting;
    }

    public Type getType() {
        return type;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public Setting<?> getSetting() {
        return this.setting;
    }

    public enum Type {
        TOGGLE_MODULE,
        SETTING_UPDATE
    }
}
