package cz.encircled.eplayer.view.listeners;

import cz.encircled.eplayer.service.action.ActionExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Encircled on 6/06/2014.
 */
public class KeyDispatcher implements KeyEventDispatcher {

    private static final Logger log = LogManager.getLogger(KeyDispatcher.class);

    private final ActionExecutor actionExecutor;

    private Map<Integer, Set<String>> binds;

    private Map<Integer, Set<String>> controlBinds;

    private Map<Integer, Set<String>> altBinds;


    public KeyDispatcher(ActionExecutor actionExecutor) {
        this.actionExecutor = actionExecutor;
        binds = new HashMap<>();
        controlBinds = new HashMap<>();
        altBinds = new HashMap<>();
    }

    public void bind(@NotNull Integer code, @NotNull String command){
        bind(code, command, false, false);
    }

    public void bind(@NotNull Integer code, @NotNull String command, boolean isControl){
        log.debug("Bind {} to {}", code, command);
        if(isControl)
            controlBinds.computeIfAbsent(code, c -> new HashSet<>()).add(command);
        else
            binds.computeIfAbsent(code, c -> new HashSet<>()).add(command);
    }

    /**
     * If is alt or control (may be both), then wont be added to normal binds
     */
    public void bind(@NotNull Integer code, @NotNull String command, boolean isControl, boolean isAlt){
        log.debug("Bind {} to {}", code, command);
        if(isControl || isAlt) {
            if (isControl)
                controlBinds.computeIfAbsent(code, c -> new HashSet<>()).add(command);
            if (isAlt)
                controlBinds.computeIfAbsent(code, c -> new HashSet<>()).add(command);
        }
        else
            binds.computeIfAbsent(code, c -> new HashSet<>()).add(command);
    }

    @Override
    public boolean dispatchKeyEvent(@NotNull KeyEvent e) {
        return e.getID() == KeyEvent.KEY_PRESSED && onKeyPressed(e);
    }

    private boolean onKeyPressed(@NotNull KeyEvent e){
        log.debug("Dispatch key pressed event with code {}", e.getKeyCode());

        boolean found = false;
        if(e.isControlDown() && controlBinds.containsKey(e.getKeyCode())) {
            controlBinds.get(e.getKeyCode()).forEach(actionExecutor::execute);
            found = true;
        }
        if(e.isAltDown() && altBinds.containsKey(e.getKeyCode())) {
            altBinds.get(e.getKeyCode()).forEach(actionExecutor::execute);
            found = true;
        }
        if(binds.containsKey(e.getKeyCode())) {
            binds.get(e.getKeyCode()).forEach(actionExecutor::execute);
            found = true;
        }
        return found;
    }

}
