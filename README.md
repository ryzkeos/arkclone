# Ark Clone

Run the same app with multiple accounts, each clone fully isolated.

## About

Ark Clone makes a real, separate copy of an installed app. Each clone gets its own package name, so it has its own data, its own login, and no link to the original or to other clones. Clones show up in the launcher like any freshly installed app.

How it works: the app's APK (and split APKs) are copied, the package name in the manifest is changed to `<original>.clone<n>`, provider authorities and custom permissions are renamed to avoid conflicts, the result is signed with a key held in the Android Keystore, and it is installed through the system installer.

Limits: apps that check their own signature, use Google Sign-In, Play Integrity, or Firebase push tied to the original package may not work in a clone.

## Installation

1. Open the project in Android Studio and run the `app` module (min sdk 24).
2. On first clone, allow Ark Clone to install unknown apps when asked.

## Usage

1. Tap clone and pick an app.
2. Confirm the system install prompt.
3. Launch the clone from Ark Clone or the launcher and sign in with a different account.
4. Deleting a clone opens the system uninstall prompt for its package.

## Credits

Author: arkeos-kzenon

## Version History

- v0.1: clones are real separate packages (repackage, sign, install)
