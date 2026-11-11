# Ark Clone

Run the Same App with Multiple Accounts, Each Clone Fully Isolated.

## About

Ark Clone makes a real, separate copy of an installed app. Each clone gets its own package name, so it has its own data, its own login, and no link to the original or to other clones. Clones show up in the launcher like any freshly installed app.

How it works: the app's APK (and split APKs) are copied, the package name in the manifest is changed to `<original>.clone<n>`, provider authorities and custom permissions are renamed to avoid conflicts, the result is signed with a key held in the Android Keystore, and it is installed through the system installer.

Limits: apps that check their own signature, use Google Sign-In, Play Integrity, or Firebase push tied to the original package may not work in a clone.

## Usage

1. Tap clone and pick an app. On the first clone, allow Ark Clone to install unknown apps when asked.
2. Confirm the system install prompt.
3. Launch the clone from Ark Clone or the launcher and sign in with a different account.
4. Deleting a clone opens the system uninstall prompt for its package.

## Credits

- Author: ryzkeos
- Published: October 7, 2026
- Built with: Kotlin, Android (min sdk 24)

## Version History

### v0.1 (2026-10-07) Real Separate Packages

- Clones are real separate packages, so each one has its own data and login.
- Repackage: the manifest package name becomes `<original>.clone<n>`, and provider authorities and custom permissions are renamed to avoid conflicts.
- Sign and install: the clone is signed with a key held in the Android Keystore and installed through the system installer.
