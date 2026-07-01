# List Manager

A simple Android app for triaging items across three lists using swipe gestures.

Swipe items in the **Inbox** to sort them into **High Priority** (swipe left) or **Low Priority** (swipe right). Use the top tabs or swipe on the tab bar / bottom edge to switch between lists.

## Navigation

Three top tabs (left to right): **Low Priority** | **Inbox** | **High Priority**

- Tap a tab to switch lists
- Swipe left/right on the tab bar or the faded bottom edge to move between lists
- The active tab is bold and full opacity; inactive tabs are faded

## Architecture

- **MVVM** — `MainActivity` observes `MainViewModel` via LiveData
- **Repository** — `ItemRepository` coordinates list operations
- **Room** — items and categories persist in a local SQLite database
- **Material 3** — dynamic color (Material You), edge-to-edge layout, dark theme support

## Build configuration

- Gradle 8.13 / Android Gradle Plugin 8.13.2
- compileSdk / targetSdk 35
- minSdk 24
- Java 17

## Swipe behavior

| Current list | Swipe left | Swipe right |
|--------------|------------|-------------|
| Inbox | Move to High Priority | Move to Low Priority |
| Low Priority | Restore to Inbox | Delete permanently |
| High Priority | Move to Inbox | Move to Inbox |

Pull to refresh (or use the toolbar action) to reset all lists to the default sample items.

## UI notes

- **Material You**: on Android 12+, the app picks up wallpaper-based dynamic colors automatically.
- **Edge-to-edge**: content extends behind the status and navigation bars with proper inset padding.
- **Day/night**: follows the system theme via `Theme.Material3.DynamicColors.DayNight`.
