package fr.orion.core.permission;

import fr.orion.api.permission.PermissionManager;
import fr.orion.api.permission.PermissionNode;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class YamlPermissionManager implements PermissionManager {
    private static final Logger logger = LoggerFactory.getLogger(YamlPermissionManager.class);

    private final Path configFile;
    private final Yaml yaml;
    private final Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> rolePermissions = new ConcurrentHashMap<>();

    public YamlPermissionManager(Path dataDirectory) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        this.yaml = new Yaml(options);
        this.configFile = dataDirectory.resolve("permissions.yml");

        try {
            Files.createDirectories(dataDirectory);
        } catch (Exception e) {
            logger.error("Failed to create permissions directory", e);
        }

        load();
    }

    @Override
    public boolean hasPermission(Member member, String permission) {
        if (member == null || permission == null) {
            return false;
        }

        if (hasPermission(member.getUser(), permission)) {
            return true;
        }

        for (Role role : member.getRoles()) {
            if (hasPermission(role, permission)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(User user, String permission) {
        if (user == null || permission == null) {
            return false;
        }

        Set<String> permissions = this.userPermissions.get(user.getId());
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        return checkPermissionMatch(permissions, permission);
    }

    @Override
    public boolean hasPermission(Role role, String permission) {
        if (role == null || permission == null) {
            return false;
        }

        Set<String> permissions = this.rolePermissions.get(role.getId());
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        return checkPermissionMatch(permissions, permission);
    }

    private boolean checkPermissionMatch(Set<String> permissions, String requiredPermission) {
        for (String perm : permissions) {
            PermissionNode node = new PermissionNode(perm);
            if (node.matches(requiredPermission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addUserPermission(String userId, String permission) {
        if (userId == null || permission == null) {
            return;
        }

        this.userPermissions.computeIfAbsent(userId, k -> new HashSet<>()).add(permission.toLowerCase());
        save();
        logger.debug("Added permission '{}' to user {}", permission, userId);
    }

    @Override
    public void removeUserPermission(String userId, String permission) {
        if (userId == null || permission == null) {
            return;
        }

        Set<String> permissions = this.userPermissions.get(userId);
        if (permissions != null) {
            permissions.remove(permission.toLowerCase());
            if (permissions.isEmpty()) {
                this.userPermissions.remove(userId);
            }
            save();
            logger.debug("Removed permission '{}' from user {}", permission, userId);
        }
    }

    @Override
    public void addRolePermission(String roleId, String permission) {
        if (roleId == null || permission == null) {
            return;
        }

        this.rolePermissions.computeIfAbsent(roleId, k -> new HashSet<>()).add(permission.toLowerCase());
        save();
        logger.debug("Added permission '{}' to role {}", permission, roleId);
    }

    @Override
    public void removeRolePermission(String roleId, String permission) {
        if (roleId == null || permission == null) {
            return;
        }

        Set<String> permissions = this.rolePermissions.get(roleId);
        if (permissions != null) {
            permissions.remove(permission.toLowerCase());
            if (permissions.isEmpty()) {
                this.rolePermissions.remove(roleId);
            }
            save();
            logger.debug("Removed permission '{}' from role {}", permission, roleId);
        }
    }

    @Override
    public Set<String> getUserPermissions(String userId) {
        Set<String> permissions = this.userPermissions.get(userId);
        return permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    @Override
    public Set<String> getRolePermissions(String roleId) {
        Set<String> permissions = this.rolePermissions.get(roleId);
        return permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    @Override
    public Set<String> getEffectivePermissions(Member member) {
        if (member == null) {
            return new HashSet<>();
        }

        Set<String> effective = new HashSet<>();

        effective.addAll(getUserPermissions(member.getId()));

        for (Role role : member.getRoles()) {
            effective.addAll(getRolePermissions(role.getId()));
        }

        return effective;
    }

    @Override
    public void clearUserPermissions(String userId) {
        if (userId != null) {
            this.userPermissions.remove(userId);
            save();
            logger.debug("Cleared all permissions for user {}", userId);
        }
    }

    @Override
    public void clearRolePermissions(String roleId) {
        if (roleId != null) {
            this.rolePermissions.remove(roleId);
            save();
            logger.debug("Cleared all permissions for role {}", roleId);
        }
    }

    @Override
    public Set<String> getAllUsersWithPermissions() {
        return new HashSet<>(this.userPermissions.keySet());
    }

    @Override
    public Set<String> getAllRolesWithPermissions() {
        return new HashSet<>(this.rolePermissions.keySet());
    }

    @Override
    public void reload() {
        load();
        logger.info("Permissions reloaded from file");
    }

    @Override
    public void save() {
        try {
            Map<String, Object> data = new HashMap<>();

            Map<String, List<String>> users = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : this.userPermissions.entrySet()) {
                users.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            Map<String, List<String>> roles = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : this.rolePermissions.entrySet()) {
                roles.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            data.put("users", users);
            data.put("roles", roles);

            try (Writer writer = Files.newBufferedWriter(this.configFile)) {
                this.yaml.dump(data, writer);
            }

            logger.debug("Permissions saved to file");
        } catch (Exception e) {
            logger.error("Failed to save permissions", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        this.userPermissions.clear();
        this.rolePermissions.clear();

        if (!Files.exists(this.configFile)) {
            createDefaultConfig();
            return;
        }

        try (InputStream is = Files.newInputStream(this.configFile)) {
            Map<String, Object> data = this.yaml.load(is);
            if (data == null) {
                createDefaultConfig();
                return;
            }

            Map<String, Object> users = (Map<String, Object>) data.get("users");
            if (users != null) {
                for (Map.Entry<String, Object> entry : users.entrySet()) {
                    String userId = entry.getKey();
                    if (entry.getValue() instanceof List) {
                        List<String> permissions = (List<String>) entry.getValue();
                        this.userPermissions.put(userId, new HashSet<>(permissions));
                    }
                }
            }

            Map<String, Object> roles = (Map<String, Object>) data.get("roles");
            if (roles != null) {
                for (Map.Entry<String, Object> entry : roles.entrySet()) {
                    String roleId = entry.getKey();
                    if (entry.getValue() instanceof List) {
                        List<String> permissions = (List<String>) entry.getValue();
                        this.rolePermissions.put(roleId, new HashSet<>(permissions));
                    }
                }
            }

            logger.info("Loaded {} user permission entries and {} role permission entries",
                    this.userPermissions.size(), this.rolePermissions.size());

        } catch (Exception e) {
            logger.error("Failed to load permissions", e);
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        logger.info("Creating default permissions configuration");
        save();
    }
}