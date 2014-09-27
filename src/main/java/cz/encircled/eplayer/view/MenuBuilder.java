package cz.encircled.eplayer.view;

import com.google.gson.reflect.TypeToken;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.util.LocalizedMessages;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Encircled on 13/09/2014.
 */
@Resource
public class MenuBuilder {

    private final static Type MENU_TYPE_TOKEN = new TypeToken<List<MenuDefinition>>() {
    }.getType();

    @Resource
    private ActionExecutor actionExecutor;

    @Resource
    private Localizations localizations;

    private Logger log = LogManager.getLogger();

    private Map<String, MenuItem> nodesByName;

    public MenuBuilder() {
        nodesByName = new HashMap<>();
    }

    public MenuItem getByName(String name) {
        return nodesByName.get(name);
    }

    private MenuBar cachedMenuBar;

    public MenuBar getFxMenu() {
        if (cachedMenuBar != null)
            return cachedMenuBar;
        MenuBar menuBar = new MenuBar();
        List<MenuDefinition> definitions;
        try {
            definitions = IOUtil.getFromJson(MenuBuilder.class.getResourceAsStream("/menubar.json"), MENU_TYPE_TOKEN);
        } catch (IOException e) {
            throw new RuntimeException("Menu build failed", e);
        }
        definitions.stream().forEach(definition -> {
            log.debug("Menu builder: next definition " + definition.label);
            Menu menu = new Menu(localizations.get(getLocalizationKey(definition.label)));
            definition.items.stream().forEach(def -> menu.getItems().add(this.getItem(def)));
            menuBar.getMenus().add(menu);
        });
        cachedMenuBar = menuBar;
        return menuBar;
    }

    private MenuItem getItem(MenuItemDefinition definition) {
        MenuItem item;
        if (definition.item != null) {
            item = new Menu();
            ((Menu) item).getItems().add(getItem(definition.item));
        } else if (definition.isMenu != null && definition.isMenu == 1) {
            item = new MenuItem();
        } else {
            item = new MenuItem();
            if (definition.action != null) {
                item.setOnAction(event -> actionExecutor.execute(getActionKey(definition.action)));
            }
        }
        item.setText(localizations.get(getLocalizationKey(definition.label)));
        if (definition.name != null) {
            nodesByName.put(definition.name, item);
            item.setId(definition.name);
        }
        if (definition.enabled != null)
            item.setDisable(definition.enabled == 0);
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
