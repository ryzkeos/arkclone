# Ark Clone

Run the Same App with Multiple Accounts, Each Clone Fully Isolated.

## About

Ark Clone makes a real, separate copy of an installed app. Each clone gets its own package name, so it has its own data, its own login, and no link to the original or to other clones. Clones show up in the launcher like any freshly installed app.

How It Works: the app's APK (and split APKs) are copied, the package name in the manifest is changed to `<original>.clone<n>`, provider authorities and custom permissions are renamed to avoid conflicts, the result is signed with a key held in the Android Keystore, and it is installed through the system installer.

Limits: apps that check their own signature, use Google Sign-In, Play Integrity, or Firebase push tied to the original package may not work in a clone.

## Usage

1. Tap clone and pick an app. On the first clone, allow Ark Clone to install unknown apps when asked.
2. Confirm the system install prompt.
3. Launch the clone from Ark Clone or the launcher and sign in with a different account.
4. Deleting a clone opens the system uninstall prompt for its package.

## Credits

- Author: ryzkeos
- Published: October 7, 2026
- Current Version: v1.0.1
- Built With: Kotlin, Android (min sdk 24)

## Version History

### v1.0.1 (2026-11-13) Light Mode Button Contrast

- Light Mode: action buttons use a softer dark gray (#555555) instead of pure black, keeping a 7.5:1 contrast ratio with white labels.
- Theme: new `action` color in the palette, applied to the launch, clone, and quick clone buttons, category filters, and the add button. Dark mode is unchanged.
- Version: versionName 1.0.1, versionCode 2.

### v1.0.0 (2026-11-13) Stable Release

- First stable release. Version name set to 1.0.0 in the app module.
- Release Build: R8 minification and resource shrinking with keep rules, and a debug application id suffix so debug and release builds install side by side.
- Build System: Gradle wrapper 9.3.1 included, so the project builds without a local Gradle install.
- Feature set and behavior carried over from v0.3 with no functional changes.

### v0.3 (2026-11-03) Suggestion Ranking

- Suggested apps are ordered by clone demand and category instead of alphabetically.

### v0.2 (2026-10-07) UI and Build Hardening

- Clone Flow: the clone dialog is restructured, can be opened directly from the suggestions list, shows install status, and drops failed installs from the clone list.
- Cloned Apps View: list and icon layouts with a long press action menu.
- Theming: light mode support, nickname display, and a split monochrome and foreground vector layer for the themed icon, which fixes a crash.
- Feedback: toast notifications are anchored at the bottom, above the navigation bar and keyboard.
- Performance: reduced startup time.
- Release Build: R8 minification and resource shrinking enabled with keep rules.
- Debug Build: separate application id suffix, so debug and release install side by side.

### v0.1 (2026-10-07) Cloning Pipeline

- Manifest Patcher: binary manifest rewrite sets the package name to `<original>.clone<n>` and renames provider authorities and custom permissions to avoid conflicts.
- Repackager: zip rewriter rebuilds base and split APKs with the patched manifest.
- Signer: clones are signed with a key held in the Android Keystore.
- Installer: clones install through the system package installer and run as standalone packages with their own data.
- Data Sources: dashboard and clone details read device and package data from the system, and the legacy runner, notification bridge, and sandbox state were removed.
- Namespace: application id and namespace set to `com.arkeos.arkclone`.
