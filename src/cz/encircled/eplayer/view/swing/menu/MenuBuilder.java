package cz.encircled.eplayer.view.swing.menu;

import com.google.gson.reflect.TypeToken;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.util.LocalizedMessages;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Encircled on 13/09/2014.
 */
public class MenuBuilder {

    private final static Type MENU_TYPE_TOKEN = new TypeToken<List<MenuDefinition>>() {
    }.getType();

    @Resource
    private ActionExecutor actionExecutor;

    @Resource
    private Localizations localizations;

    public ActionListener defaultActionListener;

    private Map<String, Component> componentsByName;

    public MenuBuilder() {
        defaultActionListener = e -> actionExecutor.execute(e.getActionCommand());
        componentsByName = new HashMap<>();
    }

    public Component getByName(String name) {
        return componentsByName.get(name);
    }

    public JMenuBar getMenu() {
        JMenuBar menuBar = new JMenuBar();

        List<MenuDefinition> definitions = null;
        try {
            definitions = IOUtil.getFromJson(MenuBuilder.class.getResourceAsStream("/menubar.json"), MENU_TYPE_TOKEN);
        } catch (IOException e) {
            throw new RuntimeException("Menu build failed", e);
        }

        definitions.stream().forEach(definition -> {
            JMenu menu = new JMenu(localizations.get(getLocalizationKey(definition.label)));
            definition.items.stream().forEach(def -> menu.add(this.getItem(def)));
            menuBar.add(menu);
        });
        return menuBar;
    }

    private JMenuItem getItem(MenuItemDefinition definition) {
        JMenuItem item = null;
        if (definition.item != null) {
            item = new JMenu();
            item.add(getItem(definition.item));
        } else if (definition.isMenu != null && definition.isMenu == 1) {
            item = new JMenu();
        } else {
            item = new JMenuItem();
            if (definition.action != null) {
                item.setActionCommand(getActionKey(definition.action));
                item.addActionListener(defaultActionListener);
            }
        }
        item.setText(localizations.get(getLocalizationKey(definition.label)));
        if (definition.name != null) {
            componentsByName.put(definition.name, item);
            item.setName(definition.name);
        }
        if (definition.enabled != null)
            item.setEnabled(definition.enabled > 0);
        return item;
    }

    private String getLocalizationKey(String key) {
        try {
            return (String) LocalizedMessages.class.getField(key).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Localization message doesn't exists - " + key);
        }
    }

    private String getActionKey(String key) {
        try {
            return (String) ActionCommands.class.getField(key).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Action command doesn't exists - " + key);
        }
    }

    public static void main(String[] args) {
        Long s = System.nanoTime();
        JMenuBar b = new MenuBuilder().getMenu();
        System.out.println((System.nanoTime() - s) / 1000000);
    }

    private class MenuDefinition {

        String label;

        Collection<MenuItemDefinition> items;

    }

    private class MenuItemDefinition {

        String label;

        String action;

        Integer enabled;

        Integer isMenu;

        String name;

        MenuItemDefinition item;

    }

}
