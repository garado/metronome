<img src="assets/images/metronome.png" alt="Metronome">

A minimal metronome app for the Light Phone 3.

## Known issues

There are latency issues when running over Bluetooth; using with speakers or wired headphones is recommended.

## Setup

```bash
# Enter development shell (if you're running Nix)
nix develop
```

```bash
bun install
bunx expo run:android
```

## Commands

```bash
bun start                 # Start dev server
bun run sync-version      # Sync version from app.json
bun run generate-icon     # Regenerate app icon
bun run generate-click    # Regenerate click sounds
```

## Acknowledgements

Thanks [Vandam](https://github.com/vandamd) for all your work!
