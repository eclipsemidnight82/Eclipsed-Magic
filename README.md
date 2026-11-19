# The Eclipse Grimoire — Forge 1.20.1 — 0.2.0-alpha (source)

**IMPORTANT: This source update has not yet been compiled or tested in Minecraft.**
Version 0.1.0-alpha compiled successfully in the user's GitHub Actions workflow, and launched in Minecraft. Version 0.2.0 adds new features that **must be compiled, launched, and tested** before distribution. Keep a backup of your existing working jar and world.

## New in this alpha

- **Lectern:** Right-click an empty *vanilla lectern* holding a grimoire to place it. Right-click the lectern to open the spellbook. Sneak + right-click the occupied lectern to remove your book. This feature is experimental and requires an in-game check, especially in multiplayer.
- **Salted door:** Right-click a vanilla wooden or iron **door** with one salt dust. An invisible ward marks the entire door (including top and bottom) and repels undead even if the door is open. Normal doors work normally for living players and animals. The ward persists with the save, and breaking the door removes it. Sneak + right-click with salt to remove your own ward; operators can remove any door's ward. The door does **not** gain a custom white overlay texture yet.
- **Salt Circle spell:** `/eclipse cast circle` (50 mana, 90s cooldown) draws an enchanted 7×7 salt ring on **level, solid ground**, without replacing other blocks or spending mana if the space is unsuitable. It blocks **all living creatures, including the caster, coven and familiars**, from stepping across the empowered salt line up to five blocks above the dust. Break a segment to make an exit. The original manually placed salt lines still repel undead only. The spell can create/place salt for you and doesn't consume items.
- **Three new regular spells:** `mend` (heals caster, nearby coven and pets); `veil` (invisibility and speed); `bind` (slows and weakens nearby hostile mobs). Existing ward, power, thunder, aegis remain.
- **Water cauldron rituals** using an ordinary vanilla water cauldron: right-click with an **amethyst shard** to give nearby allies a healing blessing; **glowstone dust** to restore 40 mana and grant night vision; or **blaze powder** to grant nearby allies and pets fire resistance. Consumes one item (except Creative) and one cauldron water level. No custom brewing machines.
- **Grimoire UI:** adds buttons for all eight spells.
- **Persistence:** Salted doors and charged circle salt positions are saved by dimension in world SavedData. Player settings and coven data continue from earlier versions.

## Commands to test

`/eclipse grimoire`

`/eclipse salt 64` (operator/cheats)

`/eclipse mana refill` (operator/cheats)

`/eclipse cast circle`

`/eclipse cast mend`

`/eclipse cast veil`

`/eclipse cast bind`

`/eclipse cast ward`

`/eclipse cast thunder`

`/eclipse coven create The Fates`

`/eclipse familiar bond`

`/eclipse color magic purple`

`/eclipse color book shadow`

## Actual gameplay boundaries of this alpha

- **The circle currently blocks living entities, not every physical object.** Thrown items, arrows, other projectiles, and teleports can still cross; flying higher than five blocks can bypass. We should not advertise it as a perfect all-object force field before implementing and testing those cases.
- The salt-door ward blocks crossing **undead bodies** in the normal doorway space; it does not block arrows fired through the door, or teleportation.
- Uncharged, manually placed salt dust still blocks undead but not ordinary players or creatures.
- As in the previous alpha, own spells skip registered coven members and protected familiars. This does not affect unrelated vanilla or other mods' damage.
- A player's movement across charged salt uses server-side position correction and may feel jerky in high-latency multiplayer. Test cautiously. Client-side lectern view is experimental.
- Terrain must be flat to cast a circle; no terrain modification, terrain replacement or lava overwriting.
- No requirement to build new complicated recipe blocks: everything uses standard vanilla items and blocks.

## Build with existing GitHub Actions

1. Upload the updated source files to your repository **at the same paths** as the original project under `eclipse_grimoire_safe_build/`.
2. Keep your working root `.github/workflows/build.yml` in place. It still works with the corrected `build.gradle` (the temporary `sed` fix becomes a no-op).
3. Commit to `main` and watch the GitHub Actions **Build The Eclipse Grimoire (Forge 1.20.1)** run.
4. If it fails, share the first error from the **Compile and package Forge mod** step so the exact API incompatibility can be fixed.
5. If it compiles, extract the resulting artifact and put only the **new `.jar`** in a *separate Forge 1.20.1 testing profile*, after removing the previous version of this mod to avoid duplicate mod IDs.

## In-game testing checklist

- Make a backup world. Launch clean Forge, verify game menu and grimoire, cast old spells before testing the new ones.
- Lectern: place grimoire, open, cast a spell, sneak-remove, quit/reload world and retrieve again.
- Cauldron: fill with water; try each of three ingredients; verify potion effects, water level decrement, and item usage.
- Door: salt the lower half of a wooden door; open it; summon a zombie outside; ensure it can't cross; test top half, reload, remove ward.
- Circle: flat 7×7 area; cast; test zombies, passive animals, other players, and the caster; remove one piece and check that entities can walk through the gap; test water washing away salt.
- Return to the old singleplayer test world and verify **no data loss**. Then test a *backed-up* multiplayer server.

**Version 0.2.0-alpha is source only until GitHub Actions reports a successful build.**
