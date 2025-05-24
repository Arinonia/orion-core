# Orion Discord Bot Core

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![JDA](https://img.shields.io/badge/JDA-5.5.1-blue.svg)](https://github.com/discord-jda/JDA)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-Orion--API-purple.svg)](https://github.com/Arinonia/orion-api)

The official implementation of the [Orion Discord Bot API](https://github.com/Arinonia/orion-api). A complete, production-ready Discord bot framework with modular architecture, comprehensive permission system, and hot-reloadable modules.

## 🚀 Features

### 🔧 **Complete Bot Implementation**
- **Ready to run** - Clone, configure, and start
- **Module system** - Hot-reloadable plugin architecture
- **Permission management** - Granular Discord-based permissions
- **Command framework** - Built-in slash commands
- **Configuration system** - YAML-based config

### 🛠️ **Management Tools**
- **`/modules`** - Enable/disable/reload modules via Discord
- **`/permission`** - Manage user and role permissions
- **Hot reloading** - Update modules without restart
- **Rich logging** - Comprehensive activity tracking

### 📦 **Included Modules**
- **Permission Management** - Complete permission control via Discord
- **Module Management** - Runtime module administration

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [Modules](#-modules)
- [Permissions](#-permissions)
- [Development](#-development)
- [Examples](#-examples)
- [Troubleshooting](#-troubleshooting)

## 🚀 Quick Start

### Prerequisites

- **Java 17+** - [Download OpenJDK](https://adoptium.net/)
- **Discord Bot Token** - [Discord Developer Portal](https://discord.com/developers/applications)
- **Git** - For cloning the repository

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/Arinonia/orion-core.git
cd orion-core

# 2. Build the project
./gradlew build

# 3. Run for first time (creates config.yml)
./gradlew run
```

### Configuration

The first run creates a `config.yml` file. Edit it with your bot details:

```yaml
bot:
  # Get your token from https://discord.com/developers/applications
  token: "YOUR_BOT_TOKEN_HERE"
  
  # Your server ID for instant command registration (optional)
  # Leave empty for global commands (takes 1 hour to register)
  guildId: "123456789012345678"
  
  # Development mode settings
  devMode: true
  logLevel: "INFO" # not implemented yet

database:
  enabled: false
  url: "jdbc:sqlite:orion.db"
  username: ""
  password: ""
```

### First Run

```bash
# Start your bot
./gradlew run

# You should see:
# [INFO] Modules: X (X enabled)
# [INFO] Commands: X registered
# [INFO] Permissions: X users, X roles
# [INFO] OrionBot started successfully
```

### Setup Permissions

Give yourself full access:

1. **Edit** `permissions/permissions.yml`:
   ```yaml
   users:
     "YOUR_DISCORD_ID":
       - "*"
   roles: {}
   ```

2. **Restart** the bot: `./gradlew run`

3. **Test** in Discord: `/permission check user:@YourName permission:*`

## 🔧 Configuration

### Bot Settings

| Setting        | Description                | Default        |
|----------------|----------------------------|----------------|
| `bot.token`    | Discord bot token          | **Required**   |
| `bot.guildId`  | Server ID for dev commands | Empty (global) |
| `bot.devMode`  | Development mode           | `true`         |
| `bot.logLevel` | Logging level              | `INFO`         |

### Command Registration

- **Guild commands** (guildId set): Register instantly, work only in that server
- **Global commands** (guildId empty): Take 1 hour to register, work everywhere

## 📦 Modules

### Module Management

```bash
# List all modules
/modules list

# Get module information
/modules info module:auto_role

# Enable/disable modules
/modules enable module:auto_role
/modules disable module:auto_role

# Reload modules (hot reload)
/modules reload module:auto_role

# System status
/modules status
```

### Installing External Modules

1. **Download** module JAR files
2. **Place** in `modules/` directory
3. **Reload** via Discord: `/modules reload module:module_name`

## 🔒 Permissions

### Permission Structure

Orion uses hierarchical permissions: `module.action`

- `auto_role.setup` - Specific permission
- `auto_role.*` - All auto-role permissions
- `*` - Global administrator access

### Managing Permissions

#### **User Permissions**
```bash
# Grant specific permission
/permission user-add user:@Username permission:auto_role.manage

# Grant module access
/permission user-add user:@Username permission:auto_role.*

# Grant full access
/permission user-add user:@Username permission:*
```

#### **Role Permissions**
```bash
# Grant permissions to roles
/permission role-add role:@Moderator permission:auto_role.*
/permission role-add role:@Admin permission:*

# Remove permissions
/permission role-remove role:@Helper permission:auto_role.manage
```

#### **Permission Queries**
```bash
# Check user permissions
/permission check user:@Username permission:auto_role.setup

# List all permissions
/permission list user:@Username
/permission list role:@Moderator

# Clear all permissions
/permission clear user:@Username
```

### Recommended Permission Setup

```bash
# Administrators - Full access
/permission role-add role:@Admin permission:*

# Moderators - Module management
/permission role-add role:@Moderator permission:auto_role.*
/permission role-add role:@Moderator permission:modules.manage

# Helpers - View only
/permission role-add role:@Helper permission:auto_role.view
/permission role-add role:@Helper permission:modules.view
```

## 🛠️ Development

### Creating Custom Modules

Create a new module by implementing the Orion API:

```java
// MyModule.java
public class MyModule extends AbstractModule {
    
    @Override
    public void onEnable() {
        // Register commands
        registerCommand(new MyCommand(this));
        
        // Register event listeners
        registerListener(new MyListener(this));
        
        getLogger().info("MyModule enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MyModule disabled!");
    }
}
```

### Module Commands with Permissions

```java
public class MyCommand extends ParentCommand {
    
    private final MyModule module;
    
    public MyCommand(MyModule module) {
        this.module = module;
        
        registerSubcommand("test", "Test command", null, new SubcommandHandler() {
            @Override
            public void execute(SlashCommandInteractionEvent event) {
                // Check permission
                if (!module.hasPermission(event.getMember(), "test")) {
                    event.reply("❌ You need `my_module.test` permission").setEphemeral(true).queue();
                    return;
                }
                
                event.reply("✅ Test successful!").queue();
            }
            
            @Override
            public SubcommandData getSubcommandData() {
                return new SubcommandData("test", "Test command");
            }
        });
    }
    
    @Override
    public String getName() { return "mycommand"; }
    
    @Override
    public String getDescription() { return "My custom command"; }
}
```

### Module Configuration

```java
public class ConfigTestModule extends YamlModuleConfig {

    public ConfigTestModule(Path rootPath) {
        super(rootPath, "config");
    }

    @Override
    protected void ensureDefaultValues() {
        if (!this.contains("welcomeMessage")) {
            this.set("welcomeMessage", "");
        }
    }

    public String getWelcomeMessage() {
        return this.getString("welcomeMessage");
    }
}

public class MyModule extends AbstractModule {

    @Override
    public void onEnable() {
        getLogger().info("MyModule enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MyModule disabled!");
    }

    @Override
    protected ModuleConfig createConfig(Path dataDirectory) {
        return new ConfigTestModule(dataDirectory);
    }

    @Override
    public ConfigTestModule getConfig() {
        return (ConfigTestModule) super.getConfig();
    }
}
```

### Building Modules

```gradle
// build.gradle for your module
plugins {
    id 'java'
}

repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Arinonia:orion-api:v0.1.0-beta'
    implementation("net.dv8tion:JDA:5.5.1")
    implementation("org.yaml:snakeyaml:2.4")
    implementation("ch.qos.logback:logback-classic:1.5.13")
   
}

jar {
    archiveBaseName = 'my-module'
    archiveVersion = '1.0.0'
}
```

Create `module.yml`:
```yaml
id: "my_module"
name: "My Awesome Module"
version: "1.0.0"
main: "com.example.MyModule"
description: "My custom module"
author: "YourName"
dependencies: []
```

## 📚 Examples

### Example 1: Welcome Module

```java
public class WelcomeModule extends AbstractModule {
    
    @Override
    public void onEnable() {
        registerListener(new WelcomeListener(this));
    }
}

public class WelcomeListener extends ListenerAdapter {
    private final WelcomeModule module;
    
    public WelcomeListener(WelcomeModule module) {
        this.module = module;
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String channelId = module.getConfig().getString("welcomeChannelId");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        
        if (channel != null) {
            String message = module.getConfig().getString("welcomeMessage");
            channel.sendMessage(message.replace("{user}", event.getMember().getAsMention())).queue();
        }
    }
}
```

### Example 2: Moderation Commands

```java
public class ModerationCommand extends ParentCommand {
    
    public ModerationCommand(ModerationModule module) {
        registerSubcommand("kick", "Kick a member", 
            subcommand -> subcommand.addOption(OptionType.USER, "user", "User to kick", true),
            new SubcommandHandler() {
                @Override
                public void execute(SlashCommandInteractionEvent event) {
                    if (!module.hasPermission(event.getMember(), "kick")) {
                        event.reply("❌ You need `moderation.kick` permission").setEphemeral(true).queue();
                        return;
                    }
                    
                    User target = event.getOption("user").getAsUser();
                    Member targetMember = event.getGuild().getMember(target);
                    
                    if (targetMember != null) {
                        targetMember.kick().queue(
                            success -> event.reply("✅ Kicked " + target.getAsMention()).queue(),
                            error -> event.reply("❌ Failed to kick member").setEphemeral(true).queue()
                        );
                    }
                }
                
                @Override
                public SubcommandData getSubcommandData() {
                    return new SubcommandData("kick", "Kick a member")
                            .addOption(OptionType.USER, "user", "User to kick", true);
                }
            }
        );
    }
}
```

## 🔍 Troubleshooting

### Common Issues

#### **Bot doesn't start**
```
ERROR: Bot token is missing!
```
**Solution:** Edit `config.yml` and add your bot token.

#### **Commands don't appear**
- **Guild commands:** Check your `guildId` in config
- **Global commands:** Wait up to 1 hour for registration

#### **Permission denied errors**
```
❌ You don't have permission to use this command
```
**Solution:**
1. Check `permissions/permissions.yml` if you have * permission


#### **Module won't load**
```
ERROR: Failed to load module: module_name
```
**Solutions:**
- Check `module.yml` syntax
- Verify main class exists
- Check dependencies
- Review logs for specific errors

### Logs and Debugging

#### **Enable debug logging** (TODO)
```yaml
# config.yml
bot:
  logLevel: "DEBUG"
```

#### **Check module status**
```bash
/modules status
/modules info module:problematic_module
```

#### **View permissions**
```bash
/permission list user:@YourName
```

### Performance Tips

- **Use guild commands** during development (instant registration)
- **Global commands** for production (wider reach)
- **Monitor logs** for performance issues
- **Restart bot** after major configuration changes

## 🚀 Production Deployment

### System Requirements

- **Java 17+** runtime environment
- **512MB RAM** minimum (1GB+ recommended)
- **50MB disk space** + space for modules and data
- **Stable internet** connection

### Running in Production

```bash
# Build production JAR
./gradlew build

# Run with production settings
java -Xmx1G -jar build/libs/orion-core-*.jar

# Or use Gradle
./gradlew run --args="--prod"
```


### Monitoring

- **Check logs** regularly for errors
- **Monitor memory** usage
- **Backup** `permissions/` and `modules-data/` directories
- **Update** modules and core regularly

## 🤝 Contributing

We welcome contributions! Here's how to help:

### Development Setup

```bash
# Fork and clone
git clone https://github.com/yourusername/orion-core.git
cd orion-core

# Create feature branch
git checkout -b feature/amazing-feature

# Make changes and test
./gradlew build
./gradlew test

# Commit and push
git commit -m "Add amazing feature"
git push origin feature/amazing-feature
```

### Code Style

- **Java 17+ features** where appropriate
- **Comprehensive JavaDoc** for public APIs
- **Unit tests** for new functionality
- **Consistent formatting** with existing code

## 📈 Roadmap

- [ ] **Web Dashboard** - Browser-based bot management
- [ ] **Database Integration** - PostgreSQL/MySQL support
- [ ] **Clustering** - Multi-instance bot support
- [ ] **Plugin Marketplace** - Community module sharing
- [ ] **REST API** - External integrations
- [ ] **Metrics Dashboard** - Performance monitoring

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **[Orion-API](https://github.com/Arinonia/orion-api)** - The core interface framework
- **[JDA](https://github.com/discord-jda/JDA)** - Java Discord API library
- **[SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml)** - YAML configuration parsing

## 📞 Support

- **📖 Documentation** - Check this README and [Orion-API docs](https://github.com/Arinonia/orion-api)
- **🐛 Bug Reports** - [Issues](https://github.com/Arinonia/orion-core/issues)
- **💬 Questions** - [Discussions](https://github.com/Arinonia/orion-core/discussions)
- **🔗 Community** - Join our [Discord Server](https://discord.gg/your-invite)

## 🔗 Related Projects

- **[Orion-API](https://github.com/Arinonia/orion-api)** - The interface framework
- **[Orion-Modules](https://github.com/Arinonia/orion-modules)** - Community modules
- **[Orion-Examples](https://github.com/Arinonia/orion-examples)** - Example implementations

---

**Ready to build amazing Discord bots?** 🎉 **Star this repo** if Orion helped you!