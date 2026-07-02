# List Manager

A simple Android app for triaging items across three lists using swipe gestures.

Swipe items in the **Inbox** to sort them into **Low Priority** (swipe left) or **High Priority** (swipe right). Swipe between columns to see adjacent lists side by side — the tab indicator and labels animate smoothly as you move.

## Navigation

Three top tabs (left to right): **Low Priority** | **Inbox** | **High Priority**

- Tap a tab to switch lists
- Swipe left/right on a column, the tab bar, or the faded bottom edge to move between lists
- Adjacent columns peek in from the sides while swiping so all three feel side by side
- The active tab is bold with a sliding underline; inactive tabs fade as you scroll

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
| Inbox | Move to Low Priority | Move to High Priority |
| Low Priority | Delete permanently | Restore to Inbox |
| High Priority | Move to Inbox | (no action) |

Pull to refresh (or use the toolbar action) to reset all lists to the default sample items.

## UI notes

- **Material You**: on Android 12+, the app picks up wallpaper-based dynamic colors automatically.
- **Edge-to-edge**: content extends behind the status and navigation bars with proper inset padding.
- **Day/night**: follows the system theme via `Theme.Material3.DynamicColors.DayNight`.
