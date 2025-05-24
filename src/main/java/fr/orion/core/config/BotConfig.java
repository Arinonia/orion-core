package fr.orion.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BotConfig {
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);
    private static final String configFile = "config.yml";

    private String token;
    private String guildId;
    private boolean devMode;
    private String logLevel;

    public BotConfig() {
        loadConfig();
    }

    private void loadConfig() {
        Path configPath = Paths.get(configFile);

        if (!Files.exists(configPath)) {
            logger.warn("Configuration file not found, creating default config.yml");
            createDefaultConfig();
            logger.error("Please configure your bot token in config.yml and restart!");
            System.exit(1);
        }

        try (InputStream is = Files.newInputStream(configPath)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(is);

            this.token = getString(config, "bot.token", "");
            this.guildId = getString(config, "bot.guildId", "");
            this.devMode = getBoolean(config, "bot.devMode", false);
            this.logLevel = getString(config, "bot.logLevel", "INFO");

            validateConfig();

        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
            System.exit(1);
        }
    }

    private void validateConfig() {
        if (token == null || token.trim().isEmpty()) {
            logger.error("Bot token is missing! Please configure 'bot.token' in config.yml");
            System.exit(1);
        }

        if (token.equals("YOUR_BOT_TOKEN_HERE")) {
            logger.error("Please replace 'YOUR_BOT_TOKEN_HERE' with your actual bot token in config.yml");
            System.exit(1);
        }

        logger.info("Configuration loaded successfully");
        logger.info("Guild ID: {}", this.guildId.isEmpty() ? "Not specified (global commands)" : this.guildId);
        logger.info("Development mode: {}", this.devMode);
    }

    private void createDefaultConfig() {
        try {
            Map<String, Object> config = new HashMap<>();

            Map<String, Object> botConfig = new HashMap<>();
            botConfig.put("token", "YOUR_BOT_TOKEN_HERE");
            botConfig.put("guildId", "");
            botConfig.put("devMode", true);
            botConfig.put("logLevel", "INFO");
            config.put("bot", botConfig);

            Map<String, Object> dbConfig = new HashMap<>();
            dbConfig.put("enabled", false);
            dbConfig.put("url", "jdbc:sqlite:orion.db");
            dbConfig.put("username", "");
            dbConfig.put("password", "");
            config.put("database", dbConfig);

            Yaml yaml = new Yaml();
            try (Writer writer = Files.newBufferedWriter(Paths.get(configFile))) {
                yaml.dump(config, writer);
            }

            logger.info("Created default configuration file: {}", configFile);

        } catch (Exception e) {
            logger.error("Failed to create default configuration", e);
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    private String getString(Map<String, Object> config, String path, String defaultValue) {
        String[] parts = path.split("\\.");
        Object current = config;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return defaultValue;
            }
        }

        return current != null ? current.toString() : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private boolean getBoolean(Map<String, Object> config, String path, boolean defaultValue) {
        String[] parts = path.split("\\.");
        Object current = config;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return defaultValue;
            }
        }

        if (current instanceof Boolean) {
            return (Boolean) current;
        }

        return defaultValue;
    }

    public String getToken() {
        return this.token;
    }

    public String getGuildId() {
        return this.guildId;
    }

    public boolean isDevMode() {
        return this.devMode;
    }

    public String getLogLevel() {
        return this.logLevel;
    }
}
