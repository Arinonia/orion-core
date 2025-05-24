package fr.orion.core.command;

import fr.orion.api.command.Command;
import fr.orion.api.command.CommandRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCommandRegistry extends ListenerAdapter implements CommandRegistry {
    private static final Logger log = LoggerFactory.getLogger(SimpleCommandRegistry.class);
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final JDA jda;
    private final String guildId;

    public SimpleCommandRegistry(JDA jda, String guildId) {
        this.jda = jda;
        this.guildId = guildId;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Command command = this.commands.get(commandName);

        if (command == null) {
            log.warn("Unknown command received: {}", commandName);
            return;
        }

        try {
            log.debug("Executing command: {}", commandName);
            command.execute(event);
        } catch (Exception e) {
            log.error("Error executing command {}: {}", commandName, e.getMessage(), e);
            event.reply("An error occurred while executing this command").setEphemeral(true).queue();
        }
    }

    @Override
    public void registerCommand(Command command) {
        if (command == null) {
            log.warn("Attempted to register a null command");
            return;
        }

        String commandName = command.getName();
        if (this.commands.containsKey(commandName)) {
            log.warn("Command {} is already registered", commandName);
            return;
        }

        this.commands.put(commandName, command);
        log.info("Command {} registered successfully", commandName);
    }

    @Override
    public void unregisterCommand(Command command) {
        if (command == null) {
            log.warn("Attempted to unregister a null command");
            return;
        }

        String commandName = command.getName();
        if (!this.commands.containsKey(commandName)) {
            log.warn("Command {} is not registered", commandName);
            return;
        }

        this.commands.remove(commandName);
        log.info("Command {} unregistered successfully", commandName);
    }

    @Override
    public Command getCommand(String name) {
        return this.commands.get(name);
    }

    @Override
    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    @Override
    public void synchronizeCommands() {
        try {
            if (this.guildId != null && !this.guildId.isEmpty()) {
                // Guild-specific commands
                Guild guild = this.jda.getGuildById(guildId);
                if (guild == null) {
                    log.error("Could not find guild with ID: {}", this.guildId);
                    return;
                }

                log.info("Synchronizing {} commands with guild: {}", this.commands.size(), guild.getName());
                guild.updateCommands().queue(success -> {
                    this.commands.values().forEach(command -> {
                        guild.upsertCommand(command.buildCommandData()).queue(
                                cmd -> log.debug("Registered command {} in guild {}", cmd.getName(), guild.getName()),
                                error -> log.error("Failed to register command in guild: {}", error.getMessage())
                        );
                    });
                });
            } else {
                // Global commands
                log.info("Synchronizing {} commands globally", this.commands.size());
                this.jda.updateCommands().queue(success -> {
                    this.commands.values().forEach(command -> {
                        this.jda.upsertCommand(command.buildCommandData()).queue(
                                cmd -> log.debug("Registered global command {}", cmd.getName()),
                                error -> log.error("Failed to register global command: {}", error.getMessage())
                        );
                    });
                });
            }
        } catch (Exception e) {
            log.error("Failed to synchronize commands: {}", e.getMessage(), e);
        }
    }
}
