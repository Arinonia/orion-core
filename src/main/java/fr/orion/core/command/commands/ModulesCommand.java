package fr.orion.core.command.commands;

import fr.orion.api.command.ParentCommand;
import fr.orion.api.interfaction.ConfirmationSystem;
import fr.orion.api.interfaction.EmbedTemplate;
import fr.orion.api.module.ModuleDescriptor;
import fr.orion.api.module.ModuleManager;
import fr.orion.api.module.Module;
import fr.orion.api.permission.PermissionManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Collection;
import java.util.List;

public class ModulesCommand extends ParentCommand {

    public ModulesCommand(ModuleManager moduleManager, PermissionManager permissionManager) {

        registerSubcommand("list", "List all modules with their status", new ListModulesCommand(moduleManager, permissionManager));
        registerSubcommand("info", "Get detailed information about a specific module",
                subcommand -> {
                    subcommand.addOption(OptionType.STRING, "module", "The module ID to get info for", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("info", "Get detailed information about a specific module")
                                .addOption(OptionType.STRING, "module", "The module ID to get info for", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "modules.view")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to view modules.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        String moduleId = event.getOption("module").getAsString();
                        Module module = moduleManager.getModule(moduleId);

                        if (module == null) {
                            event.replyEmbeds(EmbedTemplate.error("Module not found",
                                            "No module found with ID: `" + moduleId + "`").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        ModuleDescriptor descriptor = module.getModuleDescriptor();
                        String status = module.isEnabled() ? "✅ Enabled" : "❌ Disabled";

                        EmbedBuilder embed = EmbedTemplate.info(descriptor.name(), descriptor.description())
                                .addField("Status", status, true)
                                .addField("Version", descriptor.version(), true)
                                .addField("Author", descriptor.author(), true)
                                .addField("Module ID", descriptor.id(), true)
                                .addField("Main Class", descriptor.main(), true);

                        if (!descriptor.website().isEmpty()) {
                            embed.addField("Website", descriptor.website(), true);
                        }

                        if (!descriptor.license().isEmpty()) {
                            embed.addField("License", descriptor.license(), true);
                        }

                        List<String> dependencies = descriptor.dependencies();
                        if (!dependencies.isEmpty()) {
                            StringBuilder depList = new StringBuilder();
                            for (String dep : dependencies) {
                                Module depModule = moduleManager.getModule(dep);
                                String depStatus = depModule != null ? (depModule.isEnabled() ? "✅" : "❌") : "❓";
                                depList.append(depStatus).append(" `").append(dep).append("`\n");
                            }
                            embed.addField("Dependencies", depList.toString(), false);
                        }

                        List<String> softDeps = descriptor.softDependencies();
                        if (!softDeps.isEmpty()) {
                            StringBuilder softDepList = new StringBuilder();
                            for (String dep : softDeps) {
                                Module depModule = moduleManager.getModule(dep);
                                String depStatus = depModule != null ? (depModule.isEnabled() ? "✅" : "❌") : "❓";
                                softDepList.append(depStatus).append(" `").append(dep).append("` (optional)\n");
                            }
                            embed.addField("Soft Dependencies", softDepList.toString(), false);
                        }

                        event.replyEmbeds(embed.build()).queue();
                    }
                }
        );

        registerSubcommand("enable", "Enable a module",
                subcommand -> {
                    subcommand.addOption(OptionType.STRING, "module", "The module ID to enable", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("enable", "Enable a module")
                                .addOption(OptionType.STRING, "module", "The module ID to enable", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "modules.manage")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to manage modules.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        String moduleId = event.getOption("module").getAsString();
                        Module module = moduleManager.getModule(moduleId);

                        if (module == null) {
                            event.replyEmbeds(EmbedTemplate.error("Module not found",
                                            "No module found with ID: `" + moduleId + "`").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        if (module.isEnabled()) {
                            event.replyEmbeds(EmbedTemplate.warning("Module already enabled",
                                            "Module `" + moduleId + "` is already enabled.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        boolean success = moduleManager.enableModule(moduleId);

                        if (success) {
                            event.replyEmbeds(EmbedTemplate.success("Module enabled",
                                    "Module `" + moduleId + "` has been enabled successfully.").build()).queue();
                        } else {
                            event.replyEmbeds(EmbedTemplate.error("Failed to enable module",
                                            "Failed to enable module `" + moduleId + "`. Check console for details.").build())
                                    .setEphemeral(true).queue();
                        }
                    }
                }
        );

        registerSubcommand("disable", "Disable a module",
                subcommand -> {
                    subcommand.addOption(OptionType.STRING, "module", "The module ID to disable", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("disable", "Disable a module")
                                .addOption(OptionType.STRING, "module", "The module ID to disable", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "modules.manage")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to manage modules.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        String moduleId = event.getOption("module").getAsString();
                        Module module = moduleManager.getModule(moduleId);

                        if (module == null) {
                            event.replyEmbeds(EmbedTemplate.error("Module not found",
                                            "No module found with ID: `" + moduleId + "`").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        if (!module.isEnabled()) {
                            event.replyEmbeds(EmbedTemplate.warning("Module already disabled",
                                            "Module `" + moduleId + "` is already disabled.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        String moduleName = module.getModuleDescriptor().name();

                        ConfirmationSystem.ConfirmationMessage confirmation = ConfirmationSystem.createConfirmation(
                                "Are you sure you want to disable the module **" + moduleName + "** (`" + moduleId + "`)?",
                                confirmEvent -> {
                                    boolean success = moduleManager.disableModule(moduleId);

                                    if (success) {
                                        confirmEvent.editMessageEmbeds(
                                                EmbedTemplate.success("Module disabled",
                                                        "Module `" + moduleId + "` has been disabled successfully.").build()
                                        ).setComponents().queue();
                                    } else {
                                        confirmEvent.editMessageEmbeds(
                                                EmbedTemplate.error("Failed to disable module",
                                                        "Failed to disable module `" + moduleId + "`. Check console for details.").build()
                                        ).setComponents().queue();
                                    }
                                },
                                cancelEvent -> {
                                    cancelEvent.editMessageEmbeds(
                                            EmbedTemplate.info("Action cancelled",
                                                    "Module `" + moduleId + "` was not disabled.").build()
                                    ).setComponents().queue();
                                }
                        );

                        event.replyEmbeds(confirmation.embed())
                                .addActionRow(confirmation.confirmButton(), confirmation.cancelButton())
                                .queue();
                    }
                }
        );

        // Reload module
        registerSubcommand("reload", "Reload a module",
                subcommand -> {
                    subcommand.addOption(OptionType.STRING, "module", "The module ID to reload", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("reload", "Reload a module")
                                .addOption(OptionType.STRING, "module", "The module ID to reload", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "modules.reload")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to reload modules.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        String moduleId = event.getOption("module").getAsString();
                        Module module = moduleManager.getModule(moduleId);

                        if (module == null) {
                            event.replyEmbeds(EmbedTemplate.error("Module not found",
                                            "No module found with ID: `" + moduleId + "`").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        String moduleName = module.getModuleDescriptor().name();

                        ConfirmationSystem.ConfirmationMessage confirmation = ConfirmationSystem.createConfirmation(
                                "Are you sure you want to reload the module **" + moduleName + "** (`" + moduleId + "`)?\n" +
                                        "This will disable the module, unload it, reload it from disk, and enable it again.",
                                confirmEvent -> {
                                    boolean success = moduleManager.reloadModule(moduleId);

                                    if (success) {
                                        confirmEvent.editMessageEmbeds(
                                                EmbedTemplate.success("Module reloaded",
                                                        "Module `" + moduleId + "` has been reloaded successfully.").build()
                                        ).setComponents().queue();
                                    } else {
                                        confirmEvent.editMessageEmbeds(
                                                EmbedTemplate.error("Failed to reload module",
                                                        "Failed to reload module `" + moduleId + "`. Check console for details.").build()
                                        ).setComponents().queue();
                                    }
                                },
                                cancelEvent -> {
                                    cancelEvent.editMessageEmbeds(
                                            EmbedTemplate.info("Action cancelled",
                                                    "Module `" + moduleId + "` was not reloaded.").build()
                                    ).setComponents().queue();
                                }
                        );

                        event.replyEmbeds(confirmation.embed())
                                .addActionRow(confirmation.confirmButton(), confirmation.cancelButton())
                                .queue();
                    }
                }
        );

        // Status overview
        registerSubcommand("status", "Show overall module system status",
                null,
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("status", "Show overall module system status");
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "modules.view")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to view modules.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        Collection<Module> allModules = moduleManager.getModules();
                        Collection<Module> enabledModules = moduleManager.getEnabledModules();

                        int totalModules = allModules.size();
                        int enabled = enabledModules.size();
                        int disabled = totalModules - enabled;

                        EmbedBuilder embed = EmbedTemplate.info("Module System Status",
                                        "Overview of the module system")
                                .addField("Total Modules", String.valueOf(totalModules), true)
                                .addField("Enabled", String.valueOf(enabled), true)
                                .addField("Disabled", String.valueOf(disabled), true);

                        if (totalModules > 0) {
                            StringBuilder recentActivity = new StringBuilder();
                            int count = 0;
                            for (Module module : allModules) {
                                if (count >= 5) break;
                                ModuleDescriptor descriptor = module.getModuleDescriptor();
                                String status = module.isEnabled() ? "✅" : "❌";
                                recentActivity.append(status).append(" **").append(descriptor.name())
                                        .append("** `v").append(descriptor.version()).append("`\n");
                                count++;
                            }

                            if (totalModules > 5) {
                                recentActivity.append("... and ").append(totalModules - 5).append(" more");
                            }

                            embed.addField("Modules", recentActivity.toString(), false);
                        }

                        embed.setFooter("Use '/modules list' to see all modules");
                        event.replyEmbeds(embed.build()).queue();
                    }
                }
        );
    }

    @Override
    public String getName() {
        return "modules";
    }

    @Override
    public String getDescription() {
        return "Manage bot modules (list enable disable reload)";
    }
}
