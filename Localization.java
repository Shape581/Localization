import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Localization {
    private static JavaPlugin plugin;
    private static String fallbackLang;
    private static final Map<String, FileConfiguration> langs = new HashMap<>();

    public static void initialize(JavaPlugin plugin, String fallbackLang, String... langFiles) {
        Localization.plugin = plugin;
        Localization.fallbackLang = fallbackLang;
        File languagesDirectory = new File(plugin.getDataFolder(), "lang");
        if (!languagesDirectory.exists()) {
            languagesDirectory.mkdirs();
        }
        for (String langFile : langFiles) {
            saveLangFile(langFile);
        }
        loadLangs(languagesDirectory);
    }

    private static void saveLangFile(String fileName) {
        File file = new File(plugin.getDataFolder(), "lang/" + fileName);
        if (!file.exists()) {
            plugin.saveResource("lang/" + fileName, false);
            return;
        }
        InputStream stream = plugin.getResource("lang/" + fileName);
        YamlConfiguration currentLocale = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
        YamlConfiguration existingLocale = YamlConfiguration.loadConfiguration(file);
        boolean changed = false;
        for (String key : currentLocale.getKeys(false)) {
            if (!existingLocale.contains(key)) {
                existingLocale.set(key, currentLocale.getString(key));
                changed = true;
            }
        }
        if (changed) {
            try {
                existingLocale.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void loadLangs(File directory) {
        langs.clear();
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            loadLangFile(file);
        }
    }

    private static void loadLangFile(File langFile) {
        if (langFile.getName().endsWith(".yml")) {
            String langCode = langFile.getName().replace(".yml", "");
            if (langCode != null) {
                langs.put(langCode, YamlConfiguration.loadConfiguration(langFile));
            }
        }
    }

    public static String get(Locale locale, String key) {
        String lang = locale.getLanguage();
        FileConfiguration langConfig = langs.get(lang);
        if (langConfig == null) {
            langConfig = langs.get(fallbackLang);
        }
        if (langConfig.contains(key)) {
            return langConfig.getString(key);
        } else {
            plugin.getLogger().warning("Missing translation key: " + key + " in lang: " + lang);
        }
        return "N/A";
    }

    public static String get(Player player, String key) {
        return get(player.locale(), key);
    }

    public static String get(Locale locale, String key, Map<String, String> placeholders) {
        String lang = locale.getLanguage();
        FileConfiguration langConfig = langs.get(lang);
        if (langConfig == null) {
            langConfig = langs.get(fallbackLang);
        }
        if (langConfig.contains(key)) {
            String translation = langConfig.getString(key);
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                if (entry.getValue() == null) {
                    translation = translation.replace("%" + entry.getKey() + "%", "Err");
                    continue;
                }
                translation = translation.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            return translation;
        } else {
            plugin.getLogger().warning("Missing translation key: " + key + " in lang: " + lang);
        }
        return "";
    }

    public static String get(Player player, String key, Map<String, String> placeholders) {
        return get(player.locale(), key, placeholders);
    }

    public static String get(Locale locale, String key, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            return get(locale, key);
        } else {
            Map<String, String> placeholdersMap = new HashMap<>();
            for (int i = 0; i < placeholders.length; i += 2) {
                placeholdersMap.put(placeholders[i], placeholders[i + 1]);
            }
            return get(locale, key, placeholdersMap);
        }
    }

    public static String get(Player player, String key, String... placeHolders) {
        return get(player.locale(), key, placeHolders);
    }
}
