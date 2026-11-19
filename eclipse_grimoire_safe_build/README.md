# The Eclipse Grimoire — Forge 1.20.1 source (0.1.0-alpha)

**Status: SOURCE CODE, NOT YET A PLAYABLE RELEASE.** This folder contains a starter Forge mod, not a prebuilt `.jar`. It has **not been compiled or launched in Minecraft yet**. Do not put this ZIP in CurseForge's `mods` folder. Build and test it on a copy of your world before distributing.

## Features in the source code

- **White salt dust** as a placeable flat block with N/E/S/W line connections, redstone-style appearance. Salt blocks undead mobs by server-side movement checks. Salt is permanent until mined, replaced by water or lava, or loses supporting blocks. Rain does not remove it. No redstone output.
- **Grimoire item** with a dark 4-tab user interface: Spells, Coven, Familiars, Settings.
- **Custom settings** saved per player: personal grimoire name, magic color, book accent color. Defaults: *The Eclipse Grimoire*, Royal Purple particles, Shadow Black book UI.
- **Four starter spells**: ward, power, thunder, and Final Aegis. Vanilla potion effects, magic-dust particles, and *visual-only* lightning. Mana regen, cooldowns, self-weakness on big spells.
- **Coven** creation, invitation, acceptance, leaving and info. Saved in world data (survives restarts). All mod spell damage automatically skips current coven members and their tame pets.
- **Familiars** bond to a nearby pet you own while looking toward it. Bond data is saved on the pet.
- **Commands** to use spells and manage the mod, including a few admin shortcuts.
- **Crafting** recipes for the book and salt.

### NOT implemented yet / limitations

- No world-generated salt deposits yet (salt is crafted from sand + water bucket).
- No animations, voice incantations, skill trees, AI familiars, or additional Witcher/Merlin spell effects.
- Salt wards are checked against **mob movement through the salt's occupied ground blocks**, not a guaranteed force field against teleportation, high flyers or projectiles. Test jumping, swimming, climbing and modded mobs. Entity tags can be extended by a datapack to classify modded undead.
- Coven protection covers **this mod's spell damage**. It does not cancel ordinary vanilla explosions, lava, or unrelated mods' damage.
- No automated end-to-end Minecraft test has been run. Beta API incompatibilities or gameplay bugs may remain.

## BUILD on Windows (easiest)

1. Install **JDK 17** for development and use an internet connection.
2. Extract this ZIP.
3. Double-click `BUILD_WINDOWS.bat` (or inspect its commands first). The revised helper uses Windows CMD, curl, tar, and Java without changing PowerShell execution policy.
4. The script downloads the **official Forge 1.20.1 / 47.4.0 MDK** (containing Gradle wrapper), copies in these project sources, and runs Gradle `build`. Windows may still warn about unrecognized unsigned batch files; do not disable antivirus to bypass a warning.
5. If Gradle succeeds, the file appears under `dist/eclipse_grimoire-0.1.0-alpha.jar`. Put **only this compiled jar** into your CurseForge 1.20.1 Forge profile `mods` folder, not the ZIP.

The Minecraft/Forge Gradle dependencies are not included in the ZIP. The build can take some time while it downloads Forge and Minecraft files. If the script fails, check the reported error before using anything on a real world.

### Alternative: IntelliJ / VS Code

Download the official Forge **1.20.1-47.4.0 MDK** from https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html, extract it, copy `src`, `build.gradle`, `settings.gradle`, `gradle.properties` from this project into the MDK folder, then run `gradlew.bat build` (Windows) or `./gradlew build` (macOS/Linux). A Java 17 development kit and online Maven downloads are required.

## Key commands

| Command | Use | Permission |
| --- | --- | --- |
| `/eclipse help` | Show command help | Everyone |
| `/eclipse book` or `/eclipse grimoire` | Get your personal grimoire | Everyone |
| `/eclipse mana` | Check remaining mana | Everyone |
| `/eclipse mana refill` | Restore mana to 100 | Operator only |
| `/eclipse cast ward` | Protective effects for you, coven, bonded pets | Everyone |
| `/eclipse cast power` | Strength and speed | Everyone |
| `/eclipse cast thunder` | Alzur's Thunder-style magical attack; causes weakness | Everyone |
| `/eclipse cast aegis` | The Final Aegis; strong protection + attack + backlash | Everyone |
| `/eclipse rename My Book` | Rename your grimoire (2-32 chars) | Everyone |
| `/eclipse color magic purple` | Set particle color | Everyone |
| `/eclipse color book shadow` | Set book accent color | Everyone |
| `/eclipse coven create The Fates` | Create coven | Everyone |
| `/eclipse coven invite PlayerName` | Invite another online player | Everyone |
| `/eclipse coven join` | Accept pending invitation | Everyone |
| `/eclipse coven leave` | Leave coven | Everyone |
| `/eclipse coven info` | Current coven and member count | Everyone |
| `/eclipse familiar bond` | Bond a tamed animal you're looking toward | Everyone |
| `/eclipse salt 64` | Give yourself 64 salt | Operator only |

Color options: `purple`, `crimson`, `blue`, `emerald`, `gold`, `white`, `shadow`, `rose`.

### Crafting

- **Salt (x16):** 2 sand + 1 water bucket, shapeless.
- **Grimoire:** one column of amethyst shard, book, ink sac in a crafting table.

### Testing checklist

1. Launch **client and dedicated server**, verify Forge finds `eclipse_grimoire` with no errors.
2. Craft/command-grant salt and grimoire. Confirm textures and creative tabs work.
3. Lay salt across a doorway; test zombies, skeletons, husks; attempt to jump over it.
4. Check rain doesn't remove salt; apply water bucket then lava bucket and ensure both remove salt.
5. Right-click book. Verify spells, rename, two independent color choices, re-open and relog persistence.
6. Make coven with two players, summon enemies, cast area lightning, confirm allies and pets take no mod spell damage.
7. Restart the server; confirm coven memberships, pet bonds and grimoire settings persist.
8. Test with the full modpack only **after** solo tests, on a backed-up world.

### Compatibility and credits

Minecraft 1.20.1 + Forge 47.4.0 + Java 17 for development. Both the dedicated server and multiplayer clients must install the same compiled mod `.jar`. This is an original fan-inspired magic mod, not an official project of any television or game franchise. Uses Minecraft's vanilla effects/assets by reference; original salt and book icons are included.

License: MIT (source); Minecraft/Forge assets and APIs are governed by their respective terms.
