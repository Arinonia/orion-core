package fr.orion.core.command.commands;

import fr.orion.api.command.ParentCommand;
import fr.orion.api.interfaction.EmbedTemplate;
import fr.orion.api.module.ModuleDescriptor;
import fr.orion.api.module.ModuleManager;
import fr.orion.api.permission.PermissionManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Collection;

public class ListModulesCommand implements ParentCommand.SubcommandHandler {

    private final ModuleManager moduleManager;
    private final PermissionManager permissionManager;

    public ListModulesCommand(ModuleManager moduleManager, PermissionManager permissionManager) {
        this.moduleManager = moduleManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!this.permissionManager.hasPermission(event.getMember(), "modules.view")) {
            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                            "You don't have permission to view modules.").build())
                    .setEphemeral(true).queue();
            return;
        }

        Collection<fr.orion.api.module.Module> modules = this.moduleManager.getModules();

        if (modules.isEmpty()) {
            event.replyEmbeds(EmbedTemplate.info("No modules",
                    "No modules are currently loaded.").build()).queue();
            return;
        }

        EmbedBuilder embed = EmbedTemplate.info("Loaded Modules",
                "Total: " + modules.size() + " modules");

        StringBuilder enabledList = new StringBuilder();
        StringBuilder disabledList = new StringBuilder();

        for (fr.orion.api.module.Module module : modules) {
            ModuleDescriptor descriptor = module.getModuleDescriptor();
            String status = module.isEnabled() ? "✅" : "❌";
            String line = status + " **" + descriptor.name() + "** `v" + descriptor.version() + "`\n";

            if (module.isEnabled()) {
                enabledList.append(line);
            } else {
                disabledList.append(line);
            }
        }

        if (enabledList.length() > 0) {
            embed.addField("Enabled Modules", enabledList.toString(), false);
        }

        if (disabledList.length() > 0) {
            embed.addField("Disabled Modules", disabledList.toString(), false);
        }

        embed.setFooter("Use '/modules info <module>' for detailed information");
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public SubcommandData getSubcommandData() {
        return new SubcommandData("list", "List all modules with their status");
    }
}
