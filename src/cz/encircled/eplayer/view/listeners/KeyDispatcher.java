package cz.encircled.eplayer.view.listeners;

import cz.encircled.eplayer.service.action.ActionExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Encircled on 6/06/2014.
 */
public class KeyDispatcher implements KeyEventDispatcher {

    private static final Logger log = LogManager.getLogger(KeyDispatcher.class);

    private final ActionExecutor actionExecutor;

    private Map<Integer, Set<String>> codeBinds;


    public KeyDispatcher(ActionExecutor actionExecutor) {
        this.actionExecutor = actionExecutor;
        codeBinds = new HashMap<>();
    }

    public void bind(@NotNull Integer code, @NotNull String command){
        log.debug("Bind {} to {}", code, command);
        codeBinds.computeIfAbsent(code, c -> new TreeSet<>()).add(command);
    }

    @Override
    public boolean dispatchKeyEvent(@NotNull KeyEvent e) {
        return e.getID() == KeyEvent.KEY_PRESSED && onKeyPressed(e);
    }

    private boolean onKeyPressed(@NotNull KeyEvent e){
        log.debug("Dispatch key pressed event with code {}", e.getKeyCode());
        if(codeBinds.containsKey(e.getKeyCode())) {
            codeBinds.get(e.getKeyCode()).forEach(command -> actionExecutor.execute(command));
            return true;
        }
        return false;
    }

}
