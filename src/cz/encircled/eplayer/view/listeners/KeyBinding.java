package cz.encircled.eplayer.view.listeners;

import java.awt.*;

/**
 * Created by Administrator on 13.6.2014.
 */
public class KeyBinding {

    private Integer code;

    private boolean isControl;

    private boolean isAlt;

    private String command;

    private boolean propagateEvent;

    private Component component;

    public KeyBinding(Integer code, String command, Component component, boolean isControl, boolean isAlt, boolean propagateEvent) {
        this.code = code;
        this.component = component;
        this.isControl = isControl;
        this.isAlt = isAlt;
        this.command = command;
        this.propagateEvent = propagateEvent;
    }

    public Integer getCode() {
        return code;
    }

    public boolean isControl() {
        return isControl;
    }

    public boolean isAlt() {
        return isAlt;
    }

    public String getCommand() {
        return command;
    }

    public boolean isPropagateEvent() {
        return propagateEvent;
    }

    public Component getComponent() {
        return component;
    }
}
