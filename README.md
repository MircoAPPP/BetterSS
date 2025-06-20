# ScreenShare Plugin

A comprehensive Minecraft Spigot plugin for managing screenshare sessions with advanced freeze functionality, staff controls, and administrative tools.

## 📋 Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
- [Usage Guide](#usage-guide)
- [Custom World Setup](#custom-world-setup)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)

## ✨ Features

### Core Features
- **Screenshare Sessions**: Create isolated screenshare environments with custom worlds
- **Player Freeze System**: Comprehensive freeze functionality with movement, command, and inventory restrictions
- **Staff Interface**: Interactive clickable buttons for quick actions during screenshare
- **Real-time Scoreboard**: Live session information display for both staff and suspects
- **Custom Spawn Points**: Set specific spawn locations for staff and suspects in screenshare world

### Administrative Tools
- **Temporary Ban System**: Time-based banning with flexible duration formats
- **Ban Information**: Detailed ban status and history lookup
- **Duplicate IP Detection**: Find accounts sharing the same IP address
- **Chat Isolation**: Separate chat channels during screenshare sessions
- **Session Logging**: Comprehensive logging of all plugin activities

### Advanced Features
- **Custom World Support**: Use your own pre-built screenshare worlds
- **Automatic World Management**: Creates flat worlds if no custom world is provided
- **Player Reconnection Handling**: Maintains freeze status across disconnections
- **Staff Controls Interface**: Quick-action buttons for common screenshare outcomes
- **IP Tracking**: Automatic IP logging for duplicate account detection

## 🚀 Installation

### Requirements
- **Minecraft Server**: Spigot/Paper 1.20+
- **Java Version**: 17 or higher
- **Dependencies**: None (standalone plugin)

### Installation Steps

1. **Download the Plugin**
   ```bash
   # Download the latest release
   wget https://github.com/your-repo/screenshare-plugin/releases/latest/screenshare-1.0.0-all.jar
   ```

2. **Install the Plugin**
   ```bash
   Copy to your server's plugins directory
   ```

3. **Start Your Server**
   ```bash
   # The plugin will generate default configuration files
   java -jar spigot-1.20.4.jar
   ```

4. **Configure Permissions**
   - Grant appropriate permissions to staff members
   - See [Permissions](#permissions) section for details

## 🎮 Commands

### Primary Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/ss <player>` | Start a screenshare session | `screenshare.use` | `/ss PlayerName` |
| `/ss end <player>` | End a screenshare session | `screenshare.use` | `/ss end PlayerName` |
| `/freeze <player>` | Freeze/unfreeze a player | `screenshare.freeze` | `/freeze PlayerName` |
| `/tempban <player> <time> <reason> [-s]` | Temporarily ban a player | `screenshare.tempban` | `/tempban PlayerName 30d Cheating -s` |

### Administrative Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/baninfo <player>` | View detailed ban information | `screenshare.baninfo` | `/baninfo PlayerName` |
| `/dupeip <player>` | Check for duplicate IP addresses | `screenshare.dupeip` | `/dupeip PlayerName` |
| `/ssspawn <option>` | Manage screenshare spawn points | `screenshare.setspawn` | `/ssspawn staff` |

### Spawn Management Commands

| Command | Description |
|---------|-------------|
| `/ssspawn staff` | Set staff spawn location |
| `/ssspawn target` | Set suspect spawn location |
| `/ssspawn both` | Set both spawns to current location |
| `/ssspawn info` | Show current spawn locations |
| `/ssspawn reset` | Reset all spawns to world spawn |

### Time Format Examples

| Format | Description | Example |
|--------|-------------|---------|
| `1m` | Minutes | `30m` = 30 minutes |
| `1h` | Hours | `2h` = 2 hours |
| `1d` | Days | `7d` = 7 days |
| `1w` | Weeks | `2w` = 2 weeks |

## 🔐 Permissions

### Core Permissions

```yaml
screenshare.use:
  description: Start and end screenshare sessions
  default: op

screenshare.freeze:
  description: Freeze and unfreeze players
  default: op

screenshare.tempban:
  description: Temporarily ban players
  default: op

screenshare.setspawn:
  description: Set spawn locations in screenshare world
  default: op

screenshare.baninfo:
  description: View detailed ban information
  default: op

screenshare.dupeip:
  description: Check for duplicate IP addresses
  default: op
```

### Special Permissions

```yaml
screenshare.tempban.exempt:
  description: Immunity to temporary bans
  default: false

screenshare.tempban.notify:
  description: Receive tempban notifications
  default: op

screenshare.*:
  description: All screenshare permissions
  default: op
```

### Permission Setup Examples

**LuckPerms:**
```bash
# Grant all screenshare permissions to moderators
lp group moderator permission set screenshare.*

# Grant specific permissions
lp user PlayerName permission set screenshare.use
lp user PlayerName permission set screenshare.freeze

# Make a player exempt from tempbans
lp user VIPPlayer permission set screenshare.tempban.exempt
```

**PermissionsEx:**
```yaml
groups:
  moderator:
    permissions:
      - screenshare.*
  admin:
    permissions:
      - screenshare.*
      - screenshare.tempban.exempt
```

## ⚙️ Configuration

### Main Configuration (`config.yml`)

The plugin generates a comprehensive configuration file with the following sections:

#### Plugin Settings
```yaml
plugin:
  prefix: "&6[ScreenShare] &r"
  world:
    name: "screenshare_world"
    type: "FLAT"
    difficulty: "PEACEFUL"
    pvp: false
    time: 6000
    weather: false
    mob_spawning: false
```

#### Message Customization
All messages are fully customizable with color code support:

```yaml
messages:
  screenshare:
    session_started_staff: "&aScreenshare session started with {player}"
    session_started_suspect: "&eYou were taken to screenshare by {staff}"
    do_not_disconnect: "&c&lDO NOT DISCONNECT or you will be banned!"
```

#### Scoreboard Configuration
```yaml
scoreboard:
  title: "&6▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
  header: "&6┃ &l&6SCREENSHARE INFO &6┃"
  duration: "&e⏰ Duration: &f{duration}"
  staff_controls: "&6📋 STAFF CONTROLS:"
```

### Spawn Configuration (`spawns.yml`)

Custom spawn locations are stored separately:

```yaml
staff-spawn:
  world: screenshare_world
  x: 0.0
  y: 64.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0

target-spawn:
  world: screenshare_world
  x: 10.0
  y: 64.0
  z: 0.0
  yaw: 180.0
  pitch: 0.0
```

## 📖 Usage Guide

### Starting a Screenshare Session

1. **Initiate Session**
   ```
   /ss SuspiciousPlayer
   ```

2. **What Happens:**
   - Both players are teleported to the screenshare world
   - Suspect is put in Adventure mode
   - Real-time scoreboard appears for both players
   - Staff receives interactive control interface
   - Chat is isolated between staff and suspect

3. **Staff Interface**
   - `[CHEATING]` - 30-day ban for cheating
   - `[ADMITTING]` - 15-day ban for admission
   - `[CLEAR]` - End the session (player is clean)
   - `[FREEZE]` - Freeze/unfreeze the suspect

### Managing Frozen Players

1. **Freeze a Player**
   ```
   /freeze PlayerName
   ```

2. **Freeze Effects:**
   - Cannot move (position locked)
   - Cannot use most commands
   - Receives periodic reminders

3. **Automatic Handling:**
   - Freeze status persists through disconnections
   - Automatic unfreezing when session ends

### Using Temporary Bans

1. **Basic Tempban**
   ```
   /tempban PlayerName 7d Cheating
   ```

2. **Silent Tempban**
   ```
   /tempban PlayerName 30d Hacking -s
   ```

3. **Check Ban Information**
   ```
   /baninfo PlayerName
   ```

### Setting Up Custom Spawns

1. **Enter Screenshare World**
   ```
   /ss AnyPlayer
   ```

2. **Set Spawn Locations**
   ```
   /ssspawn staff    # Set staff spawn to current location
   /ssspawn target   # Set suspect spawn to current location
   /ssspawn both     # Set both spawns to current location
   ```

3. **View Current Settings**
   ```
   /ssspawn info
   ```

## 🌍 Custom World Setup

### Using Pre-built Worlds

1. **Create Your World**
   - Build your screenshare world in single-player or with WorldEdit
   - Design it with appropriate spawn areas for staff and suspects

2. **Install Custom World**
   ```bash
   # Copy your world folder to the plugin directory
   cp -r YourWorldFolder /server/plugins/ScreenShare/screenshare_world/
   ```

3. **Restart Server**
   - The plugin will automatically detect and load your custom world
   - If no custom world is found, a flat world will be generated

4. **Set Custom Spawns**
   ```
   /ss player          # Enter the world
   /ssspawn staff      # Set staff spawn
   /ssspawn target     # Set suspect spawn
   ```

### World Requirements

- **World Name**: Must be named `screenshare_world` in the plugin folder
- **Spawn Areas**: Should have designated areas for staff and suspects
- **Size**: Keep it reasonably small for better performance

## 🔧 API Documentation

### For Plugin Developers

#### Getting Plugin Instance
```java
ScreenSharePlugin plugin = (ScreenSharePlugin) Bukkit.getPluginManager().getPlugin("ScreenShare");
```

#### Session Management
```java
// Check if player is in session
boolean inSession = plugin.getSessionManager().isInSession(player);

// Get session information
ScreenShareSession session = plugin.getSessionManager().getSession(player);

// Start a session programmatically
boolean success = plugin.getSessionManager().startSession(suspect, staff);

// End a session
boolean ended = plugin.getSessionManager().endSession(suspect);
```

#### Freeze Management
```java
// Freeze a player
plugin.getFreezeManager().freezePlayer(player);

// Unfreeze a player
plugin.getFreezeManager().unfreezePlayer(player);

// Check freeze status
boolean frozen = plugin.getFreezeManager().isFrozen(player);
```

#### World Management
```java
// Get screenshare world
World ssWorld = plugin.getWorldManager().getScreenShareWorld();

// Check if world is screenshare world
boolean isSSWorld = plugin.getWorldManager().isScreenShareWorld(world);

// Get spawn locations
Location staffSpawn = plugin.getWorldManager().getStaffSpawn();
Location targetSpawn = plugin.getWorldManager().getTargetSpawn();
```

### Events

The plugin fires custom events that other plugins can listen to:

```java
// Session start event
@EventHandler
public void onSessionStart(ScreenShareSessionStartEvent event) {
    Player suspect = event.getSuspect();
    Player staff = event.getStaff();
    // Handle session start
}

// Session end event
@EventHandler
public void onSessionEnd(ScreenShareSessionEndEvent event) {
    Player suspect = event.getSuspect();
    // Handle session end
}
```

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

### Quick Start for Contributors

1. **Fork the Repository**
   ```bash
   git clone https://github.com/your-username/screenshare-plugin.git
   cd screenshare-plugin
   ```

2. **Set Up Development Environment**
   ```bash
   # Ensure you have Java 17+ and Gradle
   ./gradlew build
   ```

3. **Make Your Changes**
   - Follow the existing code style
   - Add tests for new features
   - Update documentation

4. **Submit Pull Request**
   - Create a feature branch
   - Make your changes
   - Submit a pull request with detailed description

### Development Setup

```bash
# Clone the repository
git clone https://github.com/your-repo/screenshare-plugin.git
cd screenshare-plugin

# Build the plugin
./gradlew build

# The compiled JAR will be in build/libs/
ls build/libs/screenshare-*.jar
```

## 📝 Changelog

### Version 1.0.0
- Initial release
- Core screenshare functionality
- Freeze system implementation
- Temporary ban system
- Custom world support
- Staff interface with clickable buttons
- Comprehensive configuration system

## 🐛 Bug Reports

Please report bugs using the [GitHub Issues](https://github.com/your-repo/screenshare-plugin/issues) page.

Include:
- Server version (Spigot/Paper)
- Plugin version
- Java version
- Detailed description of the issue
- Steps to reproduce
- Any error messages from console

## 💬 Support

- **Discord**: Coming soon
- **GitHub Issues**: [Report bugs or request features](https://github.com/your-repo/screenshare-plugin/issues)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spigot Team** - For the excellent server software
- **Contributors** - Everyone who has contributed to this project
- **Community** - For feedback and suggestions

---

**Author**: Scalamobile  
**Version**: 1.0.0  
**Last Updated**: 2025

