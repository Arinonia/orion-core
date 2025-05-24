package fr.orion.core.command.commands;

import fr.orion.api.command.ParentCommand;
import fr.orion.api.interfaction.ConfirmationSystem;
import fr.orion.api.interfaction.EmbedTemplate;
import fr.orion.api.permission.PermissionManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Set;

public class PermissionCommand extends ParentCommand {

    public PermissionCommand(PermissionManager permissionManager) {

        registerSubcommand("user-add", "Add a permission to a user",
                subcommand -> {
                    subcommand.addOption(OptionType.USER, "user", "The user to add permission to", true);
                    subcommand.addOption(OptionType.STRING, "permission", "The permission to add (e.g., moderation.kick)", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("user-add", "Add a permission to a user")
                                .addOption(OptionType.USER, "user", "The user to add permission to", true)
                                .addOption(OptionType.STRING, "permission", "The permission to add", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "permissions.manage")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to manage permissions.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        User user = event.getOption("user").getAsUser();
                        String permission = event.getOption("permission").getAsString().toLowerCase();

                        permissionManager.addUserPermission(user.getId(), permission);

                        event.replyEmbeds(EmbedTemplate.success("Permission added",
                                        "Permission `" + permission + "` has been added to " + user.getAsMention()).build())
                                .queue();
                    }
                }
        );

        registerSubcommand("user-remove", "Remove a permission from a user",
                subcommand -> {
                    subcommand.addOption(OptionType.USER, "user", "The user to remove permission from", true);
                    subcommand.addOption(OptionType.STRING, "permission", "The permission to remove", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("user-remove", "Remove a permission from a user")
                                .addOption(OptionType.USER, "user", "The user to remove permission from", true)
                                .addOption(OptionType.STRING, "permission", "The permission to remove", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "permissions.manage")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to manage permissions.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        User user = event.getOption("user").getAsUser();
                        String permission = event.getOption("permission").getAsString().toLowerCase();

                        permissionManager.removeUserPermission(user.getId(), permission);

                        event.replyEmbeds(EmbedTemplate.success("Permission removed",
                                        "Permission `" + permission + "` has been removed from " + user.getAsMention()).build())
                                .queue();
                    }
                }
        );

        registerSubcommand("role-add", "Add a permission to a role",
                subcommand -> {
                    subcommand.addOption(OptionType.ROLE, "role", "The role to add permission to", true);
                    subcommand.addOption(OptionType.STRING, "permission", "The permission to add", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("role-add", "Add a permission to a role")
                                .addOption(OptionType.ROLE, "role", "The role to add permission to", true)
                                .addOption(OptionType.STRING, "permission", "The permission to add", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "permissions.manage")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to manage permissions.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        Role role = event.getOption("role").getAsRole();
                        String permission = event.getOption("permission").getAsString().toLowerCase();

                        permissionManager.addRolePermission(role.getId(), permission);

                        event.replyEmbeds(EmbedTemplate.success("Permission added",
                                        "Permission `" + permission + "` has been added to " + role.getAsMention()).build())
                                .queue();
                    }
                }
        );

        registerSubcommand("role-remove", "Remove a permission from a role",
                subcommand -> {
                    subcommand.addOption(OptionType.ROLE, "role", "The role to remove permission from", true);
                    subcommand.addOption(OptionType.STRING, "permission", "The permission to remove", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("role-remove", "Remove a permission from a role")
                                .addOption(OptionType.ROLE, "role", "The role to remove permission from", true)
                                .addOption(OptionType.STRING, "permission", "The permission to remove", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "permissions.manage")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to manage permissions.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        Role role = event.getOption("role").getAsRole();
                        String permission = event.getOption("permission").getAsString().toLowerCase();

                        permissionManager.removeRolePermission(role.getId(), permission);

                        event.replyEmbeds(EmbedTemplate.success("Permission removed",
                                        "Permission `" + permission + "` has been removed from " + role.getAsMention()).build())
                                .queue();
                    }
                }
        );

        registerSubcommand("list", "List permissions for a user or role",
                subcommand -> {
                    subcommand.addOption(OptionType.USER, "user", "The user to list permissions for", false);
                    subcommand.addOption(OptionType.ROLE, "role", "The role to list permissions for", false);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("list", "List permissions for a user or role")
                                .addOption(OptionType.USER, "user", "The user to list permissions for", false)
                                .addOption(OptionType.ROLE, "role", "The role to list permissions for", false);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "permissions.view")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to view permissions.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        User user = event.getOption("user") != null ? event.getOption("user").getAsUser() : null;
                        Role role = event.getOption("role") != null ? event.getOption("role").getAsRole() : null;

                        if (user == null && role == null) {
                            event.replyEmbeds(EmbedTemplate.error("Invalid arguments",
                                            "You must specify either a user or a role.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        EmbedBuilder embed;
                        Set<String> permissions;

                        if (user != null) {
                            permissions = permissionManager.getUserPermissions(user.getId());
                            embed = EmbedTemplate.info("User Permissions",
                                    "Permissions for " + user.getAsMention());
                        } else {
                            permissions = permissionManager.getRolePermissions(role.getId());
                            embed = EmbedTemplate.info("Role Permissions",
                                    "Permissions for " + role.getAsMention());
                        }

                        if (permissions.isEmpty()) {
                            embed.addField("Permissions", "No permissions assigned", false);
                        } else {
                            StringBuilder permList = new StringBuilder();
                            for (String perm : permissions) {
                                permList.append("• `").append(perm).append("`\n");
                            }
                            embed.addField("Permissions (" + permissions.size() + ")", permList.toString(), false);
                        }

                        event.replyEmbeds(embed.build()).queue();
                    }
                }
        );

        registerSubcommand("check", "Check if a user has a specific permission",
                subcommand -> {
                    subcommand.addOption(OptionType.USER, "user", "The user to check", true);
                    subcommand.addOption(OptionType.STRING, "permission", "The permission to check", true);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("check", "Check if a user has a specific permission")
                                .addOption(OptionType.USER, "user", "The user to check", true)
                                .addOption(OptionType.STRING, "permission", "The permission to check", true);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "permissions.view")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to view permissions.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        User user = event.getOption("user").getAsUser();
                        String permission = event.getOption("permission").getAsString();

                        Member member = event.getGuild().getMember(user);
                        if (member == null) {
                            event.replyEmbeds(EmbedTemplate.error("User not found",
                                            "User is not a member of this server.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        boolean hasPermission = permissionManager.hasPermission(member, permission);

                        EmbedBuilder embed = hasPermission ?
                                EmbedTemplate.success("Permission Check",
                                        user.getAsMention() + " **has** permission `" + permission + "`") :
                                EmbedTemplate.error("Permission Check",
                                        user.getAsMention() + " **does not have** permission `" + permission + "`");

                        event.replyEmbeds(embed.build()).queue();
                    }
                }
        );

        registerSubcommand("clear", "Clear all permissions for a user or role",
                subcommand -> {
                    subcommand.addOption(OptionType.USER, "user", "The user to clear permissions for", false);
                    subcommand.addOption(OptionType.ROLE, "role", "The role to clear permissions for", false);
                },
                new SubcommandHandler() {
                    @Override
                    public SubcommandData getSubcommandData() {
                        return new SubcommandData("clear", "Clear all permissions for a user or role")
                                .addOption(OptionType.USER, "user", "The user to clear permissions for", false)
                                .addOption(OptionType.ROLE, "role", "The role to clear permissions for", false);
                    }

                    @Override
                    public void execute(SlashCommandInteractionEvent event) {
                        if (!permissionManager.hasPermission(event.getMember(), "permissions.manage")) {
                            event.replyEmbeds(EmbedTemplate.error("Permission denied",
                                            "You don't have permission to manage permissions.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        User user = event.getOption("user") != null ? event.getOption("user").getAsUser() : null;
                        Role role = event.getOption("role") != null ? event.getOption("role").getAsRole() : null;

                        if (user == null && role == null) {
                            event.replyEmbeds(EmbedTemplate.error("Invalid arguments",
                                            "You must specify either a user or a role.").build())
                                    .setEphemeral(true).queue();
                            return;
                        }

                        String target = user != null ? user.getAsMention() : role.getAsMention();

                        ConfirmationSystem.ConfirmationMessage confirmation = ConfirmationSystem.createConfirmation(
                                "Are you sure you want to clear all permissions for " + target + "?",
                                confirmEvent -> {
                                    if (user != null) {
                                        permissionManager.clearUserPermissions(user.getId());
                                    } else {
                                        permissionManager.clearRolePermissions(role.getId());
                                    }

                                    confirmEvent.editMessageEmbeds(
                                            EmbedTemplate.success("Permissions cleared",
                                                    "All permissions have been cleared for " + target).build()
                                    ).setComponents().queue();
                                },
                                cancelEvent -> {
                                    cancelEvent.editMessageEmbeds(
                                            EmbedTemplate.info("Action cancelled",
                                                    "No permissions were cleared.").build()
                                    ).setComponents().queue();
                                }
                        );

                        event.replyEmbeds(confirmation.embed())
                                .addActionRow(confirmation.confirmButton(), confirmation.cancelButton())
                                .queue();
                    }
                }
        );
    }

    @Override
    public String getName() {
        return "permission";
    }

    @Override
    public String getDescription() {
        return "Manage user and role permissions";
    }
}