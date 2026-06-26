# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

NoteKeeper — a Google Keep–style Android notes app (Jetpack Compose, single module `:app`, package `com.ahoura.notekeeper`). Kotlin 2.4, AGP 9.2, `compileSdk 37`, `minSdk 26`, Java 11.

## Commands

The Gradle wrapper is `./gradlew` (use `gradlew.bat` from cmd; `./gradlew` works in the Bash tool). No standalone lint config beyond the Android defaults.

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # build + install on a connected device/emulator
./gradlew test                   # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest   # instrumented tests (needs a device/emulator; app/src/androidTest)
./gradlew lint                   # Android lint

# Single unit test class / method:
./gradlew test --tests "com.ahoura.notekeeper.ExampleUnitTest"
./gradlew test --tests "com.ahoura.notekeeper.ExampleUnitTest.someMethod"
```

Dependencies are managed through the version catalog at `gradle/libs.versions.toml` — add/bump libraries there and reference them as `libs.*` in `app/build.gradle.kts`, not with hardcoded coordinates.

## Architecture

Clean Architecture in three layers under `app/src/main/java/com/ahoura/notekeeper/`, with a one-directional dependency flow `presentation → domain ← data`.

- **`domain/`** — pure Kotlin, no Android/framework imports. `model/Note.kt` (the `Note` domain model + `NoteColor` palette enum), `repository/NoteRepository.kt` (interface), and one class per operation in `usecase/` (e.g. `GetAllNotesUseCase`, `TogglePinNoteUseCase`). Use cases expose `operator fun invoke(...)` and are the only thing ViewModels call into.
- **`data/`** — Room persistence. `local/` holds `NoteDatabase`, `NoteDao`, and `entity/NoteEntity`. `repository/NoteRepositoryImpl` implements the domain interface; `repository/NoteMappers.kt` converts between `NoteEntity` and `Note`.
- **`presentation/`** — one package per feature (`home`, `editor`, `search`, `archive`), each with a `@HiltViewModel` ViewModel + `*UiState` + Compose `*Screen`. `presentation/common/UiState.kt` is the shared `Loading/Success/Error` wrapper.
- **`ui/`** — reusable Compose components (`ui/components/`) and theming (`ui/theme/`, including `NoteColors.kt` which maps `NoteColor` → Compose colors per light/dark theme). `navigation/` holds the `Screen` sealed route definitions and `AppNavGraph`.

### Domain ↔ entity boundary (important)

The domain `Note` and the persisted `NoteEntity` are deliberately different shapes — all translation lives in `NoteMappers.kt`:
- Timestamps are `java.time.LocalDateTime` in the domain, **epoch millis** in the entity.
- `labels` is a `List<String>` in the domain, a **JSON array string** (`labelsJson`) in the entity, encoded with an explicit `kotlinx.serialization` `ListSerializer` (no `@Serializable` plugin annotations).
- `color` is a `NoteColor` enum in the domain, stored as its `hexValue` string (`colorHex`); resolve back via `NoteColor.fromHex`.

When adding a field to a note, update **both** data classes and both mapper directions.

### State & data flow

- The DB is the single source of truth. DAO reads return `Flow<List<NoteEntity>>`; repositories map them to `Flow<List<Note>>`; ViewModels turn them into a `StateFlow<*UiState>` via `.map → .catch → .stateIn(SharingStarted.WhileSubscribed(5_000))`. UI-only state (layout mode, multi-select set) lives in separate `MutableStateFlow`s and is merged with `combine`. See `HomeViewModel` for the canonical pattern.
- One-shot UI effects (snackbars, e.g. undo-delete) go through a `Channel(Channel.BUFFERED)` exposed as `receiveAsFlow()`, not through state.
- Sort/filter logic lives in **SQL** (`NoteDao`): active notes exclude archived and order `isPinned DESC, updatedAt DESC`; search is a `LIKE` over title+content. Prefer extending DAO queries over filtering in Kotlin.

### DI (Hilt)

`@HiltAndroidApp` is on `NoteKeeperApp`; `MainActivity` is `@AndroidEntryPoint`. Two `SingletonComponent` modules in `di/`: `DatabaseModule` (`@Provides` the Room DB + DAO) and `RepositoryModule` (`@Binds` impl → interface). ViewModels get use cases injected; screens obtain ViewModels with `hiltViewModel()`.

### Navigation

`Screen` (sealed class in `navigation/`) defines routes; the editor is parameterized as `editor?noteId={noteId}` with `NEW_NOTE_ID = -1L` as the "create new" sentinel — build routes with `Screen.Editor.createRoute(id)`. Per-destination enter/exit transitions are defined in `AppNavGraph`.

## Notes & gotchas

- Room uses `fallbackToDestructiveMigration(dropAllTables = true)` — schema changes wipe the local DB rather than migrate. Add real migrations before this matters for real data.
- An empty note (`Note.isEmpty`: blank title *and* content) is discarded on save rather than persisted.
