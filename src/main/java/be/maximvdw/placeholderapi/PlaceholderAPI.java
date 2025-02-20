package be.maximvdw.placeholderapi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import be.maximvdw.placeholderapi.events.PlaceholderAddedEvent;
import be.maximvdw.placeholderapi.internal.CustomPlaceholdersPack;
import be.maximvdw.placeholderapi.internal.PlaceholderPack;
import be.maximvdw.placeholderapi.internal.PlaceholderPlugin;
import be.maximvdw.placeholderapi.internal.ui.SendConsole;
import be.maximvdw.placeholderapi.internal.utils.DateUtils;
import be.maximvdw.placeholderapi.internal.utils.NumberUtils;
import be.maximvdw.placeholderapi.internal.utils.bukkit.BukkitUtils;
import be.maximvdw.placeholderapi.internal.utils.chat.ColorUtils;

/**
 * MVdWPlaceholderAPI
 *
 * @author Maxim Van de Wynckel (Maximvdw)
 */
public class PlaceholderAPI extends JavaPlugin {
    /* Placeholder container */
    private static List<PlaceholderPlugin> placeholderPlugins = new ArrayList<>();
    /* Custom placeholders registered in the API */
    private static PlaceholderPack customPlaceholders = null;
    /* Placeholder change listeners */
    private static List<PlaceholderAddedEvent> placeholderAddedHandlers = new ArrayList<PlaceholderAddedEvent>();

    @Override
    public void onEnable() {
        super.onEnable();
        new SendConsole(this);
        SendConsole.info("Initializing ...");
        customPlaceholders = new CustomPlaceholdersPack(this);

        // Prevent linkage errors
        new ColorUtils();
        new BukkitUtils();
        new DateUtils();
        new NumberUtils();

    }

    @Override
    public void onDisable() {

    }

    /**
     * Register an MVdW Plugin
     *
     * @param plugin            Plugin
     * @param placeholderPlugin Placeholder plugin container
     * @return success
     */
    public boolean registerMVdWPlugin(Plugin plugin, PlaceholderPlugin placeholderPlugin) {
        if (customPlaceholders == null)
            customPlaceholders = new CustomPlaceholdersPack(this);
        if (!placeholderPlugins.contains(placeholderPlugin)) {
            placeholderPlugin.registerPlaceHolder(customPlaceholders);
            placeholderPlugins.add(placeholderPlugin);
            SendConsole.info("Hooked into MVdW plugin: " + plugin.getName());
            return true;
        } else {
            placeholderPlugin.registerPlaceHolder(customPlaceholders);
            SendConsole.info("Hooked into MVdW plugin again: " + plugin.getName());
            return false;
        }
    }

    /**
     * Replace placeholders in input
     *
     * @param offlinePlayer Player to replace placeholders for
     * @param input         Placeholder format {placeholder}
     * @return Return result with replaced placeholders
     */
    public static String replacePlaceholders(OfflinePlayer offlinePlayer, String input) {
        if (placeholderPlugins.size() == 0) {
            SendConsole.warning("There is no MVdW placeholder plugin installed!");
            SendConsole.warning("Put one of Maximvdw's premium placeholder plugins in the server!");

            return input;
        }
        return placeholderPlugins.get(0).getPlaceholderResult(input,
                offlinePlayer);
    }

    /**
     * Returns the amount of placeholders loaded into the memory
     *
     * @return Placeholder count
     */
    public static int getLoadedPlaceholderCount() {
        if (placeholderPlugins.size() == 0) {
            SendConsole.warning("There is no MVdW placeholder plugin installed!");
            SendConsole.warning("Put on of Maximvdw's premium placeholder plugins in the server!");
            return 0;
        }
        return placeholderPlugins.get(0).getPlaceHolderCount();
    }

    /**
     * Register a custom placeholder
     *
     * @param plugin      Plugin that is registering the placeholder
     * @param placeholder Placeholder to be registered WITHOUT { }
     * @return Returns if the placeholder is added or not
     */
    public static boolean registerPlaceholder(Plugin plugin, String placeholder, PlaceholderReplacer replacer) {
        if (plugin == null)
            return false;
        if (placeholder == null)
            return false;
        if (placeholder.equals(""))
            return false;
        if (replacer == null)
            return false;
        SendConsole.info(plugin.getName() + " added custom placeholder {"
                + placeholder.toLowerCase() + "}");
        customPlaceholders.addOfflinePlaceholder(
                placeholder,
                "Custom MVdWPlaceholderAPI placeholder",
                false,
                new be.maximvdw.placeholderapi.internal.PlaceholderReplacer<String>(String.class,
                        replacer) {
                    @Override
                    public String getResult(String placeholder,
                            OfflinePlayer player) {
                        PlaceholderReplacer replacer = (PlaceholderReplacer) getArguments()[0];
                        PlaceholderReplaceEvent event = new PlaceholderReplaceEvent(
                                player, placeholder);
                        return replacer.onPlaceholderReplace(event);
                    }
                });
        for (PlaceholderAddedEvent event : placeholderAddedHandlers) {
            if (event != null)
                event.onPlaceholderAdded(plugin, placeholder.toLowerCase(), replacer);
        }
        return true; // Placeholder registered
    }

    /**
     * Register a static custom placeholder
     *
     * @param plugin      Plugin that is registering the placeholder
     * @param placeholder Placeholder to be registered WITHOUT { }
     * @param value       Placeholder value
     * @return Returns if the placeholder is added or not
     */
    public static boolean registerStaticPlaceholders(Plugin plugin, String placeholder, final String value) {
        if (plugin == null)
            return false;
        if (placeholder == null)
            return false;
        if (placeholder.equals(""))
            return false;
        PlaceholderReplacer replacer = new PlaceholderReplacer() {
            @Override
            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                return value;
            }
        };
        customPlaceholders.addOfflinePlaceholder(
                placeholder,
                "Custom MVdWPlaceholderAPI placeholder",
                false,
                new be.maximvdw.placeholderapi.internal.PlaceholderReplacer<String>(String.class,
                        replacer) {
                    @Override
                    public String getResult(String placeholder,
                            OfflinePlayer player) {
                        PlaceholderReplacer replacer = (PlaceholderReplacer) getArguments()[0];
                        PlaceholderReplaceEvent event = new PlaceholderReplaceEvent(
                                player, placeholder);
                        return replacer.onPlaceholderReplace(event);
                    }
                });
        for (PlaceholderAddedEvent event : placeholderAddedHandlers) {
            if (event != null)
                event.onPlaceholderAdded(plugin, placeholder.toLowerCase(), replacer);
        }
        return true; // Placeholder registered
    }

    /**
     * Add a placeholder listener
     *
     * @param handler placeholder added handler
     */
    public void addPlaceholderListener(PlaceholderAddedEvent handler) {
        placeholderAddedHandlers.add(handler);
    }
}
