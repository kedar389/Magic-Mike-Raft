# Commands Cheatsheet

Quick reference for the common operations in this repo. For the *why* behind each
workflow, see the other files in `docs/`. Commands are grouped by task.

> **Tooling status (2026-07-12):** `packwiz`, a JDK, and `pre-commit` are **not yet
> installed** in this environment. Commands below that need them are marked ⛔ until set up.
> Install notes are at the bottom.

---

## Adding a new mod  ⛔ needs packwiz

The modlist lives in `pack/` as one packwiz pack. Each mod is tagged
`side = client | server | both`, and packwiz derives the two lists from it:
the **server** installs `server + both`, players' **`.mrpack`** gets `client + both`.

```powershell
cd pack

# 1. Install the mod (prefer Modrinth; fall back to CurseForge)
packwiz modrinth install <slug-or-url>       # e.g. packwiz modrinth install jei
packwiz curseforge install <slug-or-url>     # if it's CurseForge-only

# 2. Set the side — EDIT the generated file, there is no CLI flag:
#    open  pack/mods/<mod>.pw.toml  and set:
#        side = "both"      # content: custom blocks/items/entities  (DEFAULT — safe)
#        side = "server"    # server-only: vanilla-block structures, worldgen, perf/util
#        side = "client"    # client-only: rendering, JEI, minimap, sound, particles, UI
#    ⚠ If a structure/content mod adds CUSTOM blocks it MUST be "both",
#      or clients get missing textures / mismatch kicks.

# 3. Regenerate the index
packwiz refresh

# 4. (later) update CHANGELOG.md and export — see below
```

Other packwiz operations (run inside `pack/`):

```powershell
packwiz list                    # list all mods in the pack
packwiz update <mod>            # update one mod
packwiz update --all           # update everything
packwiz remove <mod>           # remove a mod
packwiz url add <name> <url>   # add a mod from a raw URL (not on MR/CF)
```

---

## Exporting the pack for players  ⛔ needs packwiz (+ JDK for the compat jar)

```powershell
# The scripted path: builds the compat mod jar, refreshes, and exports both formats
.\scripts\export_pack.ps1        # (created in Step 6 — not yet written)

# Manual equivalent, from pack/
packwiz refresh
packwiz modrinth export          # -> *.mrpack  (import into Prism Launcher)
packwiz curseforge export        # -> *.zip
```

---

## Syncing configs & datapacks to the live server  ✅ works now

```powershell
.\scripts\sync_server.ps1 -DryRun    # preview what would be copied (no changes)
.\scripts\sync_server.ps1            # actually copy
```

Copies (additive overwrite, never deletes):
`mod_configs_server/` → `server/config/` · `datapacks/` → `server/<level-name>/datapacks/` ·
`server_configuration/server.properties` → `server/` · `flags/server.txt` → `server/user_jvm_args.txt`

---

## Installing server mods from the pack  ⛔ needs packwiz + JDK

```powershell
# Serve the pack locally (HTTP; file:// is unreliable)
cd pack
packwiz serve                    # serves http://localhost:8080/pack.toml

# In the server/ dir, pull server-side mods:
java -jar packwiz-installer-bootstrap.jar -g -s server http://localhost:8080/pack.toml
```

---

## Building / testing the compat mod  ⛔ needs JDK 17

Run from `mod_compats/OriginSpellSchools/`:

```bash
./gradlew build             # compile + jar (Error Prone runs during compile)
./gradlew test              # JUnit unit tests
./gradlew spotlessApply     # auto-format the code
./gradlew spotlessCheck checkstyleMain   # QC checks only (no format changes)
./gradlew runClient         # launch a dev client with the mod loaded
./gradlew runServer         # launch a dev server
./gradlew runGameTestServer # headless GameTests; exit code = failed test count
```

> **JDK note:** the mod *builds* on **JDK 17** (ForgeGradle 6 requirement). The live
> *server* runs on **Java 21** (needed for the `-XX:+ZGenerational` flag). Both can be installed.

---

## Running the server  ✅ works now (needs Java 21)

```bat
cd server
run.bat
```

---

## pre-commit hooks  ⛔ needs Python + pre-commit

```bash
pipx install pre-commit        # one-time
pre-commit install             # enable hooks in this repo
pre-commit run --all-files     # run all hooks manually
```

---

## Installing the tooling

```powershell
winget install EclipseAdoptium.Temurin.17.JDK   # JDK 17 (build the mod)
winget install GoLang.Go                          # then: go install github.com/packwiz/packwiz@latest
# packwiz can also be grabbed as a prebuilt Windows binary from its GitHub Actions / nightly.link
# Python: install a real Python (not the Store alias), then: python -m pip install pipx
```
