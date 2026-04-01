![server-banner example](example.png)

# server-banner

>**S**cala **E**ngineered **R**enderer **V**astly **E**xceeding **R**equirements\
**B**ecause **A**SCII **N**ever **N**eeded **E**xtensive **R**ationale

A Scala Native CLI tool that generates ASCII art server banners with FIGlet-style Slant font rendering, decorative scroll borders, and optional greeting text. Compiles to a single native binary — no JVM required.

## Build

Requires [SBT](https://www.scala-sbt.org/) and [LLVM/Clang](https://scala-native.org/en/stable/user/setup.html) (for Scala Native compilation).

```bash
# Release build (produces a native binary)
sbt release

# Standalone fat JAR (requires a Java 17+ runtime)
sbt targetJVM/release
```

Release artifacts are written to the `release/` directory.

## Test

```bash
sbt test
```

## Usage

```
Usage: server-banner [OPTIONS] <banner-text>

Arguments:
  <banner-text>            Text to render as ASCII art banner

Options:
  --version                Print version and exit
  --help                   Show this help message
  --greeting <text>        Greeting text displayed above the banner
  --color <detect|on|off>  Color output mode (default: detect)
```

## Examples

```bash
# Default demo banner + help
server-banner --help

# Banner only, no ANSI coloring
server-banner --color off My-Server

# Banner with greeting
server-banner --greeting 'Such  a  *lovely*  place' HT-Cal-4N
```

## Installing as a Linux MOTD

The server banner can be displayed when users log into a Linux server via SSH.

### 1. Build the Linux binary

Cross-compile on Linux (or build directly on the target server):

```bash
sbt release
```

### 2. Move it so that it's locally available

```bash
sudo mv release/server-banner /usr/local/bin/
```

### 3. Generate the banner

```bash
server-banner --color on --greeting "Such a *lovely* place" HT-Cal-4N | sudo tee /etc/motd
```

This writes the static banner text into `/etc/motd`, which is displayed by the SSH daemon on login.
Note on the usage of tee to elevate writing to `/etc/motd`, as well as forcing color on a pipe.

### 4. Verify

```bash
ssh user@server
```

You should see the banner on login:

```
                                                              .---.
                                                             /  .  \
        S U C H   A   * L O V E L Y *   P L A C E           |\_/|  /|
  __________________________________________________________|___|_' |
 /  .-.        __  ________    ______      __      __ __  _   __    |
|  /   \      / / / /_  __/   / ____/___ _/ /     / // / / | / /    |
| |\_.  |    / /_/ / / /_____/ /   / __ `/ /_____/ // /_/  |/ /     |
|\|  | /|   / __  / / /_____/ /___/ /_/ / /_____/__  __/ /|  /      |
| `---' |  /_/ /_/ /_/      \____/\__,_/_/        /_/ /_/ |_/       |
|       |__________________________________________________________/
|       |
 \     /
  `---'
```

### Troubleshooting

- **Banner not showing on SSH login**: Ensure `PrintMotd yes` is set in `/etc/ssh/sshd_config` and restart sshd (`sudo systemctl restart sshd`).
- **Duplicate banners**: If you see the banner twice, you may have both `/etc/motd` and a `pam_motd.so` script active — use one or the other.

## License

[MIT](LICENSE) (c) Marko Elezovic
