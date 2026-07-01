# Linear project setup — List Manager

Use this guide to create the Linear project and import the backlog. The Linear MCP server in Cursor is **not authenticated** in the cloud agent environment, so the project must be created from your machine (or after you connect Linear in **Cursor → Settings → MCP → Linear**).

## 1. Create the project

In Linear:

1. **Projects → New project**
2. **Name:** `List Manager`
3. **Summary:** Generic Android triage app — swipe items across Low Priority, Inbox, and High Priority columns.
4. **Status:** In Progress
5. **Target date:** optional
6. **Lead:** assign yourself
7. **Description:** paste the project description below

### Project description (paste into Linear)

```
Android list-management app for triaging items across three columns using swipe gestures.

Repo: https://github.com/AndrewOCC/Swiper-Hackathon
Branch: cursor/modernize-android-deps-5b5e
PR: https://github.com/AndrewOCC/Swiper-Hackathon/pull/1

Stack: Java 17, MVVM, Room, Material 3, AndroidX, compileSdk 35.

Columns (left → right): Low Priority | Inbox | High Priority
Panel navigation: top tabs + edge swipes
Item actions: email-style swipe previews; delete from Low Priority with red/bin affordance
```

## 2. Import the backlog

**Option A — CSV import (fastest)**

1. Linear → **Settings → Workspace → Import / Export → Import**
2. Choose **CSV**
3. Upload `docs/linear-import.csv`
4. Map columns if prompted (Title, Description, Status, Priority, Labels)
5. Assign imported issues to the **List Manager** project

**Option B — Manual**

Copy issues from `docs/linear-backlog.md` into Linear one by one, or ask Cursor (with Linear MCP connected) to create them from that file.

## 3. Suggested labels

Create these workspace labels before or after import:

| Label | Color suggestion | Use for |
|-------|------------------|---------|
| `architecture` | blue | MVVM, Room, DI, Kotlin |
| `ui` | purple | Compose, Material, navigation |
| `data` | green | persistence, sync, import/export |
| `quality` | orange | tests, CI, lint |
| `release` | red | signing, Play Store, APK |
| `polish` | gray | UX refinements, a11y, haptics |

## 4. Suggested milestones (Linear project milestones)

| Milestone | Scope |
|-----------|--------|
| **M1 — Modern foundation** | Build tooling, MVVM, Room, Material 3 *(mostly done)* |
| **M2 — Core UX** | Tabs, swipe previews, settings, panel navigation *(mostly done)* |
| **M3 — User content** | Add/edit/delete items, empty states, undo |
| **M4 — Quality & release** | Tests, CI, signed release, Play Store readiness |
| **M5 — Compose & Kotlin** | Language migration and UI rewrite |

## 5. Latest APK

https://github.com/AndrewOCC/Swiper-Hackathon/releases/download/list-manager-1.3.0/list-manager-debug.apk

## 6. Re-run from Cursor (after Linear auth)

Once Linear MCP is connected, you can ask the agent:

> Create a Linear project "List Manager" and import all issues from `docs/linear-import.csv`.

The agent can then use Linear tools to create the project and issues automatically.
