# NoteKeeper

A Google Keep–style notes app for Android, built entirely with **Jetpack Compose** and **Material 3**. NoteKeeper is a clean, modern showcase of an offline-first Android architecture: notes, checklists, labels, reminders, color themes, archive & trash, full-text search, and full English/Persian (RTL) localization.

> Package `com.ahoura.notekeeper` · single Gradle module `:app`

---

## Table of contents

- [Features](#features)
- [Screens](#screens)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Data model & persistence](#data-model--persistence)
- [Theming, fonts & localization](#theming-fonts--localization)
- [Getting started](#getting-started)
- [Build & run](#build--run)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [License](#license)

---

## Features

- 📝 **Notes & checklists** — create plain notes or checkable to-do lists; empty notes are silently discarded on save.
- 📌 **Pin** important notes to the top.
- 🎨 **Per-note colors** — an 11-color palette with a polished color-picker bottom sheet (named colors, springy selection, light/dark-aware swatches).
- 🏷️ **Labels** — tag notes and create new labels inline.
- ⏰ **Reminders** — set a date/time reminder per note with a system notification.
- 🗄️ **Archive** — keep notes out of the main list without deleting them.
- 🗑️ **Trash** — soft-delete with restore; deletes show an **undo** snackbar.
- 🔍 **Search** — fast `LIKE` search across note title and content.
- 🔲 **Grid / list layouts** — toggle the home screen between a masonry grid and a single column; swipe-to-archive in list mode.
- ✅ **Multi-select** — long-press to select multiple notes and bulk pin / color / archive / delete.
- 🌗 **Theme** — Light / Dark via a dark-mode switch, with an animated palette cross-fade.
- 🌍 **Localization** — English and Persian (فارسی) with correct RTL layout and per-language fonts (Inter / Vazirmatn).
- 📴 **Offline-first** — everything is stored locally in Room; no account or network required.

## Screens

| Route | Screen | Purpose |
|-------|--------|---------|
| `home` | **Home** | All active notes (pinned first), layout toggle, multi-select, FAB to add. |
| `editor?noteId={noteId}` | **Editor** | Create/edit a note; title, content/checklist, color, labels, reminder, pin, archive. Auto-saves. |
| `search` | **Search** | Live search over title + content. |
| `archive` | **Archive** | Archived notes; restore back to home. |
| `trash` | **Trash** | Soft-deleted notes; restore or permanently delete. |
| `settings` | **Settings** | Theme (dark-mode switch) and language selection. |

> The editor is the only parameterized route. Build routes with `Screen.Editor.createRoute(id)`; `NEW_NOTE_ID = -1L` is the "create new" sentinel.

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin **2.4** |
| UI | Jetpack **Compose** (BOM `2026.02.01`), **Material 3** |
| Architecture | Clean Architecture (presentation / domain / data) + MVVM |
| DI | **Hilt** |
| Persistence | **Room** |
| Async | **Kotlinx Coroutines** + `Flow` / `StateFlow` |
| Serialization | **Kotlinx Serialization** (labels stored as JSON) |
| Navigation | **Navigation-Compose** |
| Preferences | **DataStore** (theme) + **AppCompat** locales (language) |
| Build | **AGP 9.2**, KSP, Gradle version catalog |
| SDK | `compileSdk 37` · `targetSdk 36` · `minSdk 26` · Java 11 |

## Architecture

NoteKeeper follows **Clean Architecture** in three layers with a one-directional dependency flow:

```
presentation  →  domain  ←  data
   (Compose,       (pure       (Room,
    ViewModels)     Kotlin)     repositories)
```

- **`domain/`** — pure Kotlin, no Android imports. The `Note` model, the `NoteRepository` interface, and one **use case per operation** (`GetAllNotesUseCase`, `TogglePinNoteUseCase`, …). Use cases expose `operator fun invoke(...)` and are the only thing ViewModels call into.
- **`data/`** — Room implementation: `NoteDatabase`, `NoteDao`, `NoteEntity`, plus `NoteRepositoryImpl` and `NoteMappers` that translate between the entity and the domain model.
- **`presentation/`** — one package per feature (`home`, `editor`, `search`, `archive`, `trash`, `settings`), each a `@HiltViewModel` + `*UiState` + Compose `*Screen`.

### State & data flow

- **The database is the single source of truth.** DAO reads return `Flow<List<NoteEntity>>` → repositories map to `Flow<List<Note>>` → ViewModels expose a `StateFlow<*UiState>` via `.map → .catch → .stateIn(WhileSubscribed(5s))`.
- **UI-only state** (layout mode, multi-select set) lives in separate `MutableStateFlow`s and is merged with `combine`. See `HomeViewModel` for the canonical pattern.
- **One-shot effects** (undo-delete snackbars) flow through a `Channel(Channel.BUFFERED)` exposed as `receiveAsFlow()`, not through state.
- **Sort & filter live in SQL** (`NoteDao`): active notes exclude archived/trashed and order `isPinned DESC, updatedAt DESC`; search is a `LIKE` over title + content.

### Dependency injection

`@HiltAndroidApp` on `NoteKeeperApp`; `MainActivity` is `@AndroidEntryPoint`. Two `SingletonComponent` modules:
- `DatabaseModule` — `@Provides` the Room database + DAO.
- `RepositoryModule` — `@Binds` the implementation to the `NoteRepository` interface.

## Project structure

```
app/src/main/java/com/ahoura/notekeeper/
├── NoteKeeperApp.kt          # @HiltAndroidApp
├── MainActivity.kt           # @AndroidEntryPoint, hosts the theme + nav graph
├── di/                       # DatabaseModule, RepositoryModule
├── domain/
│   ├── model/                # Note, NoteColor, ChecklistItem, ThemeMode, AppLanguage
│   ├── repository/           # NoteRepository (interface)
│   └── usecase/              # one class per operation
├── data/
│   ├── local/                # NoteDatabase, NoteDao, entity/NoteEntity
│   ├── repository/           # NoteRepositoryImpl, NoteMappers
│   ├── preferences/          # SettingsDataStore, LocaleManager
│   └── reminder/             # ReminderNotifications, scheduling
├── presentation/
│   ├── home/  editor/  search/  archive/  trash/  settings/
│   └── common/UiState.kt     # shared Loading/Success/Error wrapper
├── navigation/               # Screen routes, AppNavGraph
└── ui/
    ├── components/           # NoteCard, NoteColorPicker, StaggeredNoteGrid, …
    └── theme/                # Theme, Color, Type (Inter/Vazirmatn), NoteColors, Shape
```

## Data model & persistence

The domain `Note` and the persisted `NoteEntity` are **deliberately different shapes** — all translation lives in `NoteMappers.kt`:

| Field | Domain (`Note`) | Entity (`NoteEntity`) |
|-------|-----------------|------------------------|
| Timestamps | `java.time.LocalDateTime` | epoch **millis** (`Long`) |
| `labels` | `List<String>` | **JSON array string** (`labelsJson`) via explicit `ListSerializer` |
| `color` | `NoteColor` enum | `hexValue` string (`colorHex`), resolved with `NoteColor.fromHex` |

> ⚠️ When adding a field to a note, update **both** data classes **and both** mapper directions.

> ⚠️ Room uses `fallbackToDestructiveMigration(dropAllTables = true)` — schema changes **wipe** the local DB rather than migrate. Add real migrations before this matters for real data.

## Theming, fonts & localization

- **Themes** — a custom Google-Keep-inspired light/dark palette in `ui/theme/Color.kt`, resolved by `NoteKeeperTheme`. The light background is tuned a shade off pure white so white note cards separate cleanly. The palette cross-fades when you flip between light and dark.
- **Note colors** — `NoteColor` (domain enum) maps to Compose colors per light/dark in `ui/theme/NoteColors.kt`, with an auto-derived legible content color.
- **Fonts** — bundled and selected by locale in `ui/theme/Type.kt`:
  - **Inter** (variable font) for English / Latin.
  - **Vazirmatn** (Regular / Medium / Bold) for Persian.
- **Languages** — English + Persian. Theme preference is persisted in **DataStore**; language is owned by **AppCompat** locales (so it survives process death and Android's per-app language setting). Strings live in `res/values/` and `res/values-fa/`.

## Getting started

### Prerequisites

- **Android Studio** (latest stable; the project uses AGP 9.2 / Kotlin 2.4)
- **JDK 11+**
- An Android device or emulator running **API 26+** (Android 8.0 Oreo or newer)

### Clone

```bash
git clone <your-repo-url> NoteKeeper
cd NoteKeeper
```

Open the folder in Android Studio and let it sync, or use the Gradle wrapper from the command line.

## Build & run

The Gradle wrapper is `./gradlew` (use `gradlew.bat` from `cmd`).

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # build + install on a connected device/emulator
./gradlew lint                   # Android lint
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

> Dependencies are managed through the version catalog at `gradle/libs.versions.toml` — add/bump libraries there and reference them as `libs.*` in `app/build.gradle.kts`, **not** with hardcoded coordinates.

## Testing

```bash
./gradlew test                   # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest   # instrumented tests (needs a device/emulator)

# Single unit test class / method:
./gradlew test --tests "com.ahoura.notekeeper.ExampleUnitTest"
./gradlew test --tests "com.ahoura.notekeeper.ExampleUnitTest.someMethod"
```

## Roadmap

Ideas and known follow-ups:

- [ ] Real Room **migrations** (replace destructive fallback before shipping real data).
- [ ] Custom / arbitrary note colors (HSV picker beyond the 11 presets).
- [ ] Rich-text formatting in the editor.
- [ ] Note sharing / export.
- [ ] Widget and home-screen quick-add.
- [ ] Cloud sync / backup.

## License

No license file is currently included. Add a `LICENSE` file to declare how others may use this code.

> Bundled fonts are licensed under the **SIL Open Font License (OFL)**: [Inter](https://github.com/rsms/inter) and [Vazirmatn](https://github.com/rastikerdar/vazirmatn).

---

<p align="center">Built with ❤️ using Jetpack Compose.</p>
