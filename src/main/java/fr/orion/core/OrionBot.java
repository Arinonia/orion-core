package fr.orion.core;

import fr.orion.api.Bot;
import fr.orion.api.command.CommandRegistry;
import fr.orion.api.event.EventRegistry;
import fr.orion.api.interfaction.ConfirmationSystem;
import fr.orion.api.module.Module;
import fr.orion.api.module.ModuleManager;
import fr.orion.api.module.loader.DefaultModuleLoader;
import fr.orion.api.permission.PermissionManager;
import fr.orion.core.command.SimpleCommandRegistry;
import fr.orion.core.command.commands.ModulesCommand;
import fr.orion.core.command.commands.PermissionCommand;
import fr.orion.core.config.BotConfig;
import fr.orion.core.event.SimpleEventRegistry;
import fr.orion.core.permission.YamlPermissionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.EnumSet;

public class OrionBot implements Bot {
    private static final Logger log = LoggerFactory.getLogger(OrionBot.class);

    private final BotConfig config;

    private JDA jda;
    private ModuleManager moduleManager;
    private CommandRegistry commandRegistry;
    private EventRegistry eventRegistry;
    private PermissionManager permissionManager;

    public OrionBot() {
        log.info("Initializing OrionBot... v0.0.1");
        this.config = new BotConfig();
    }

    public void start() {
        log.info("Starting OrionBot...");

        initializeJDA();
        initializeRegistries();
        loadModules();
        registerCommands();
        logBotStatistics();

        log.info("OrionBot started successfully");
    }

    private void initializeJDA() {
        log.info("Initializing JDA...");

        try {
            JDABuilder builder = JDABuilder.createDefault(this.config.getToken())
                    .enableIntents(EnumSet.of(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS
                    ));
            this.jda = builder.build().awaitReady();
            log.info("JDA initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize JDA", e);
            throw new RuntimeException("Failed to initialize JDA", e);
        }
    }

    private void initializeRegistries() {
        log.info("Initializing registries...");

        this.eventRegistry = new SimpleEventRegistry(this.jda);
        this.commandRegistry = new SimpleCommandRegistry(this.jda, this.config.getGuildId());

        this.eventRegistry.registerListener((EventListener) this.commandRegistry);
        this.eventRegistry.registerListener(new ConfirmationSystem());

        this.permissionManager = new YamlPermissionManager(Path.of("permissions"));
        Path modulePath = Path.of("modules");
        this.moduleManager = new DefaultModuleLoader(modulePath, this);

    }

    private void loadModules() {
        log.info("Loading modules...");

        int loadedModules = this.moduleManager.loadModules();
        int enabledModules = this.moduleManager.enableModules();
        log.info("Loaded {} modules, enabled {} modules", loadedModules, enabledModules);
    }

    private void registerCommands() {
        log.info("Registering commands...");
        this.commandRegistry.registerCommand(new PermissionCommand(this.permissionManager));
        this.commandRegistry.registerCommand(new ModulesCommand(this.moduleManager, this.permissionManager));
        this.commandRegistry.synchronizeCommands();
        log.info("Commands registered successfully");
    }

    private void logBotStatistics() {
        log.info("=== Orion Bot Statistics ===");
        log.info("Guild ID: {}", this.config.getGuildId() != null ? this.config.getGuildId() : "Not specified (using global commands)");
        log.info("Modules: {} ({} enabled)",
                this.moduleManager.getModules().size(),
                this.moduleManager.getEnabledModules().size());
        log.info("Commands: {} registered", this.commandRegistry.getCommands().size());
        log.info("Permissions: {} users, {} roles",
                this.permissionManager.getAllUsersWithPermissions().size(),
                this.permissionManager.getAllRolesWithPermissions().size());
        log.info("============================");

        for (Module module : this.moduleManager.getModules()) {
            String status = module.isEnabled() ? "ENABLED" : "DISABLED";
            if (module.getModuleDescriptor() != null) {
                log.debug("Module: {} [{}] - {}",
                        module.getModuleDescriptor().name(),
                        status,
                        module.getModuleDescriptor().description());
            } else {
                log.debug("Module: {} [{}] - No descriptor available",
                        module.getClass().getSimpleName(),
                        status);
            }
        }
    }

    private void shutdown() {
        log.info("Shutting down OrionBot...");

        if (this.moduleManager != null) {
            this.moduleManager.disableModules();
        }

        if (this.jda != null) {
            this.jda.shutdown();
            log.info("JDA shutdown complete");
        }
        log.info("OrionBot shutdown completed successfully");
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return this.commandRegistry;
    }

    @Override
    public EventRegistry getEventRegistry() {
        return this.eventRegistry;
    }

    @Override
    public JDA getJDA() {
        return this.jda;
    }

    @Override
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }
}
