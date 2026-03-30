```
                                                                                   .---.
                                                                                  /  .  \
        S C A L A   N A T I V E   S E R V E R   M O T D   G E N E R A T O R      |\_/|  /|
  _______________________________________________________________________________|___|_' |
 /  .-.                                             __                                   |
|  /   \      ________  ______   _____  _____      / /_  ____ _____  ____  ___  _____    |
| |\_.  |    / ___/ _ \/ ___/ | / / _ \/ ___/_____/ __ \/ __ `/ __ \/ __ \/ _ \/ ___/    |
|\|  | /|   (__  )  __/ /   | |/ /  __/ /  /_____/ /_/ / /_/ / / / / / / /  __/ /        |
| `---' |  /____/\___/_/    |___/\___/_/        /_.___/\__,_/_/ /_/_/ /_/\___/_/         |
|       |_______________________________________________________________________________/
|       |
 \     /
  `---'
```

# server-banner

> **S**cala **E**ngineered **R**enderer **V**astly **E**xceeding **R**equirements — **B**ecause **A**SCII **N**ever **N**eeded **E**xtensive **R**ationale

A Scala Native CLI tool that generates ASCII art server banners with FIGlet-style Slant font rendering, decorative scroll borders, and optional greeting text. Compiles to a single native binary — no JVM required.

## Build

Requires [SBT](https://www.scala-sbt.org/) and [LLVM/Clang](https://scala-native.org/en/stable/user/setup.html) (for Scala Native compilation).

```bash
# Debug build (fast compilation)
sbt nativeLink

# Release build (optimized for size, UPX compressed if available)
sbt release
```

The binary is produced at `target/scala-3.8.1/server-banner`.

## Test

```bash
sbt test
```

## Usage

```bash
# Banner with greeting
server-banner --greeting 'Such  a  *lovely*  place' My-Server

# Banner only
server-banner My-Server

# Default demo banner + help
server-banner
```

```
Usage: server-banner [OPTIONS] <banner-text>

Arguments:
  <banner-text>            Text to render as ASCII art banner

Options:
  --greeting <text>        Greeting text displayed above the banner

Examples:
  server-banner My-Server
  server-banner --greeting 'Such  a  *lovely*  place' My-Server
```

## Installing as a Linux MOTD

The server banner can be displayed when users log into a Linux server via SSH.

### 1. Build the Linux binary

Cross-compile on Linux (or build directly on the target server):

```bash
sbt release
```

### 2. Copy to the server

```bash
scp target/scala-3.8.1/server-banner user@server:/usr/local/bin/server-banner
ssh user@server 'chmod +x /usr/local/bin/server-banner'
```

### 3. Generate the banner

```bash
ssh user@server 'server-banner --greeting "Such  a  *lovely*  place" HT-California-02 > /etc/motd'
```

This writes the static banner text into `/etc/motd`, which is displayed by the SSH daemon on login.

### 4. Dynamic MOTD (optional)

For banners that update dynamically (e.g., including hostname or date), create a script in `/etc/update-motd.d/` instead:

```bash
cat <<'SCRIPT' | ssh user@server 'sudo tee /etc/update-motd.d/10-server-banner && sudo chmod +x /etc/update-motd.d/10-server-banner'
#!/bin/sh
/usr/local/bin/server-banner --greeting 'Such  a  *lovely*  place' "$(hostname)"
SCRIPT
```

On systems that support `update-motd` (Debian, Ubuntu), this script runs on each login. Make sure to disable the default static MOTD if using this approach:

```bash
ssh user@server 'sudo truncate -s 0 /etc/motd'
```

### 5. Verify

```bash
ssh user@server
```

You should see the banner on login:

```
                                                                                         .---.
                                                                                        /  .  \
        S U C H   A   * L O V E L Y *   P L A C E                                      |\_/|  /|
  _____________________________________________________________________________________|___|_' |
 /  .-.        __  ________    ______      ___ ____                 _             ____ ___     |
|  /   \      / / / /_  __/   / ____/___ _/ (_) __/___  _________  (_)___ _      / __ \__ \    |
| |\_.  |    / /_/ / / /_____/ /   / __ `/ / / /_/ __ \/ ___/ __ \/ / __ `/_____/ / / /_/ /    |
|\|  | /|   / __  / / /_____/ /___/ /_/ / / / __/ /_/ / /  / / / / / /_/ /_____/ /_/ / __/     |
| `---' |  /_/ /_/ /_/      \____/\__,_/_/_/_/  \____/_/  /_/ /_/_/\__,_/      \____/____/     |
|       |_____________________________________________________________________________________/
|       |
 \     /
  `---'
```

### Troubleshooting

- **Banner not showing on SSH login**: Ensure `PrintMotd yes` is set in `/etc/ssh/sshd_config` and restart sshd (`sudo systemctl restart sshd`).
- **Dynamic MOTD not running**: Check that `pam_motd.so` is enabled in `/etc/pam.d/sshd` and that your script is executable.
- **Duplicate banners**: If you see the banner twice, you may have both `/etc/motd` and an `update-motd.d` script active — use one or the other.

## License

[MIT](LICENSE) (c) Marko Elezovic
