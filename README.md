# List Manager

A simple Android app for triaging items across three lists using swipe gestures.

Swipe items in the **Inbox** to sort them into **Starred** (swipe left) or **Archived** (swipe right). Use the navigation drawer to switch lists, restore archived items, or move starred items back to the inbox.

## Architecture

- **MVVM** — `MainActivity` observes `MainViewModel` via LiveData
- **Repository** — `ItemRepository` owns the three lists and move/delete logic
- **Local-only** — no network; sample items are seeded in memory

## Build configuration

- Gradle 8.13 / Android Gradle Plugin 8.13.2
- compileSdk / targetSdk 35
- minSdk 24
- Java 17

## Swipe behavior

| Current list | Swipe left | Swipe right |
|--------------|------------|-------------|
| Inbox | Move to Starred | Move to Archived |
| Archived | Restore to Inbox | Delete permanently |
| Starred | Move to Inbox | Move to Inbox |

Pull to refresh (or use the toolbar action) to reset all lists to the default sample items.
