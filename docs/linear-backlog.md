# List Manager — feature backlog

Structured for Linear. **Status:** Done | Backlog | Todo | In Progress

Priority: **Urgent** > **High** > **Medium** > **Low**

---

## Done

### LIS-1 — Modernize Android build toolchain
- **Status:** Done
- **Priority:** Urgent
- **Labels:** architecture, release
- Gradle 8.13, AGP 8.13.2, compileSdk/targetSdk 35, minSdk 24, Java 17, AndroidX, remove jcenter

### LIS-2 — Remove Freelancer API integration
- **Status:** Done
- **Priority:** High
- **Labels:** architecture
- Strip Retrofit/auth; generic List Manager with local sample data

### LIS-3 — MVVM + Repository architecture
- **Status:** Done
- **Priority:** High
- **Labels:** architecture
- MainViewModel, ItemRepository, LiveData, ViewModelFactory

### LIS-4 — Room persistence
- **Status:** Done
- **Priority:** High
- **Labels:** data, architecture
- SQLite via Room; items survive app restarts; seed data on first launch

### LIS-5 — Material 3 UI with edge-to-edge and Material You
- **Status:** Done
- **Priority:** High
- **Labels:** ui
- Dynamic colors, transparent system bars, NavigationView → later replaced by tabs, MaterialCardView, day/night

### LIS-6 — Top tab navigation (three columns)
- **Status:** Done
- **Priority:** High
- **Labels:** ui
- Low Priority | Inbox | High Priority tabs; active tab highlighted

### LIS-7 — Settings page for sample data
- **Status:** Done
- **Priority:** Medium
- **Labels:** ui
- Settings icon top-right; reset sample items moved off main screen

### LIS-8 — Panel swipe navigation
- **Status:** Done
- **Priority:** High
- **Labels:** ui
- Swipe tab bar, blank list areas, and bottom edge to change columns; landscape-friendly thresholds

### LIS-9 — Email-style item swipe previews
- **Status:** Done
- **Priority:** High
- **Labels:** ui
- Card slides horizontally; target column label revealed underneath; no fade on move

### LIS-10 — Delete affordance from Low Priority
- **Status:** Done
- **Priority:** High
- **Labels:** ui
- Swipe left on Low Priority → red background, bin icon, fade out, permanent delete

### LIS-11 — Correct spatial swipe directions
- **Status:** Done
- **Priority:** High
- **Labels:** ui
- Panel and item swipes: left → left column, right → right column

### LIS-12 — GitHub APK releases
- **Status:** Done
- **Priority:** Medium
- **Labels:** release
- Debug APKs published to GitHub Releases (latest v1.3.0)

---

## Backlog — Core product

### LIS-20 — Add new items (FAB + dialog)
- **Status:** Backlog
- **Priority:** High
- **Labels:** ui, data
- Floating action button opens dialog/form for title + description; persists to Inbox via Room

### LIS-21 — Edit existing items
- **Status:** Backlog
- **Priority:** High
- **Labels:** ui, data
- Tap item or long-press menu to edit title/description

### LIS-22 — Undo last swipe action
- **Status:** Backlog
- **Priority:** High
- **Labels:** ui, data
- Snackbar with Undo after move/delete; revert Room transaction

### LIS-23 — Empty state per column
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** ui
- Illustration + copy when a column has no items; hint how to swipe items in

### LIS-24 — Haptic feedback on swipe complete
- **Status:** Backlog
- **Priority:** Low
- **Labels:** polish, ui
- Light haptic on successful move; stronger on delete

### LIS-25 — Search and filter items
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** ui, data
- Search across all columns or within current column

### LIS-26 — Drag to reorder within a column
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** ui, data
- Manual priority ordering via drag handle; persist sortOrder in Room

### LIS-27 — Export / import lists
- **Status:** Backlog
- **Priority:** Low
- **Labels:** data
- JSON export/import for backup and migration

---

## Backlog — Architecture & quality

### LIS-30 — Kotlin migration
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** architecture
- Convert Java sources to Kotlin incrementally (ViewModel, Repository, UI layer)

### LIS-31 — Jetpack Compose UI rewrite
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** ui, architecture
- Replace XML layouts with Compose; Material 3 components; swipe gestures in Compose

### LIS-32 — Hilt dependency injection
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** architecture
- Inject Repository, Database, ViewModel factories

### LIS-33 — Paging 3 for large lists
- **Status:** Backlog
- **Priority:** Low
- **Labels:** data, architecture
- Room + PagingSource if lists grow beyond sample size

### LIS-34 — Unit tests (ViewModel + Repository)
- **Status:** Backlog
- **Priority:** High
- **Labels:** quality
- Test swipe logic, category moves, delete, seed/reset

### LIS-35 — UI tests (Espresso / Compose)
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** quality
- Tab switching, swipe flows, settings reset

### LIS-36 — GitHub Actions CI
- **Status:** Backlog
- **Priority:** High
- **Labels:** quality, release
- `./gradlew assembleDebug test` on every PR

### LIS-37 — Gradle version catalog
- **Status:** Backlog
- **Priority:** Low
- **Labels:** architecture, release
- Centralize dependencies in `libs.versions.toml`

---

## Backlog — Release & platform

### LIS-40 — Signed release build
- **Status:** Backlog
- **Priority:** High
- **Labels:** release
- Release keystore, R8 minification, ProGuard rules

### LIS-41 — Play Store readiness
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** release
- Privacy policy, store listing, screenshots, content rating

### LIS-42 — Predictive back gesture support
- **Status:** Backlog
- **Priority:** Low
- **Labels:** ui, polish
- Android 14+ back preview for Settings and future screens

### LIS-43 — Accessibility audit
- **Status:** Backlog
- **Priority:** Medium
- **Labels:** polish, ui
- Content descriptions, TalkBack order, swipe action announcements, contrast

### LIS-44 — Fix README swipe direction table
- **Status:** Backlog
- **Priority:** Low
- **Labels:** polish
- README still has outdated Inbox swipe directions vs implemented behavior

### LIS-45 — Per-app language support
- **Status:** Backlog
- **Priority:** Low
- **Labels:** ui, polish
- Android 13+ locale preferences; extract all strings

---

## Icebox

### LIS-50 — Home screen widget
- **Status:** Backlog
- **Priority:** Low
- **Labels:** ui
- Show Inbox count; tap opens app

### LIS-51 — Notifications / reminders
- **Status:** Backlog
- **Priority:** Low
- **Labels:** data
- Remind about stale Inbox items

### LIS-52 — Multi-device sync
- **Status:** Backlog
- **Priority:** Low
- **Labels:** data, architecture
- Cloud backend or sync adapter — only if product scope expands
