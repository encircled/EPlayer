package cz.encircled.eplayer.view.swing.listeners;

import cz.encircled.eplayer.service.action.ActionExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Encircled on 6/06/2014.
 */
public class KeyDispatcher implements KeyEventDispatcher {

    private static final Logger log = LogManager.getLogger(KeyDispatcher.class);

    @Resource
    private ActionExecutor actionExecutor;

    private Map<Integer, List<KeyBinding>> bindings;

    public KeyDispatcher() {
        bindings = new HashMap<>();
    }

    public void bind(@NotNull KeyBinding keyBinding) {
        bindings.computeIfAbsent(keyBinding.getCode(), c -> new ArrayList<>()).add(keyBinding);
    }

    public static KeyBinding globalControlKey(@NotNull Integer code, @NotNull String command) {
        return new KeyBinding(code, command, null, true, false, false);
    }

    public static KeyBinding globalAltKey(@NotNull Integer code, @NotNull String command) {
        return new KeyBinding(code, command, null, false, true, false);
    }

    public static KeyBinding globalKey(@NotNull Integer code, @NotNull String command) {
        return new KeyBinding(code, command, null, false, false, true);
    }

    public static KeyBinding focusedOnlyKey(@NotNull Integer code, @NotNull String command, @NotNull Component component) {
        return new KeyBinding(code, command, component, false, false, false);
    }

    @Override
    public boolean dispatchKeyEvent(@NotNull KeyEvent e) {
        boolean propagate = true;
        if (e.getID() == KeyEvent.KEY_PRESSED)
            propagate = onKeyPressed(e);
        return false;
    }

    private boolean onKeyPressed(@NotNull KeyEvent e) {
        log.debug("Dispatch key pressed event with code {}", e.getKeyCode());
        if (bindings.containsKey(e.getKeyCode())) {
            for (KeyBinding binding : filterKeyBindings(bindings.get(e.getKeyCode()), e)) {
                actionExecutor.execute(binding.getCommand());
                if (!binding.isPropagateEvent())
                    break;
            }
        }
        return false;
    }

    private List<KeyBinding> filterKeyBindings(List<KeyBinding> bindings, KeyEvent event) {
        return bindings.stream()
                .filter(binding -> {
                    boolean matches = true;
                    if (binding.isAlt() && !event.isAltDown())
                        matches = false;
                    else if (binding.isControl() && !event.isControlDown())
                        matches = false;
                    else if (binding.getComponent() != null && !binding.getComponent().equals(event.getComponent()))
                        matches = false;
                    return matches;
                }).collect(Collectors.toList());
    }

}
