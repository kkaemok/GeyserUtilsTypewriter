# GeyserUtilsTypewriter

A standalone Typewriter `0.9.0-beta-177` extension providing:

- a reusable Java/Bedrock platform branch;
- native Bedrock NPC dialogue through GeyserUtils;
- native Bedrock NPC option dialogue through GeyserUtils.

The Java branch deliberately has no BetterHUD dependency. Connect it to your existing BetterHUD Spoken dialogue in the Typewriter editor.

## Build

The checked-in GeyserUtils artifacts are compiled from GeyserUtils commit `0539202` with both patches in `patches/` applied. Patch `0001` adds direct Java entity-ID binding. Patch `0002` restores the real `NpcRequestPacket` decoder for each Bedrock session so native NPC responses reach GeyserUtils on Geyser `2.9.4-b1081`.

```powershell
.\gradlew.bat clean build
```

Install `build/libs/GeyserUtilsTypewriter-1.0.0.jar` in `plugins/Typewriter/extensions/`.

Required Paper plugins are Typewriter `0.9.0-beta-177`, GeyserUtils Spigot, and floodgate. The matching GeyserUtils Geyser extension must be installed in Geyser's `extensions/` directory; install the GeyserUtils Velocity bridge on Velocity when that proxy topology is used. Typewriter's Entity extension is also required.

## Entries

`Platform Check` exposes `Java Triggers` and `Bedrock Triggers`. Connect the Java output to BetterHUD Spoken and the Bedrock output to either native NPC dialogue entry.

Both NPC entries use an explicit target mode:

- `TYPEWRITER_ENTITY` resolves the Java entity ID per viewer from `AudienceEntityDisplay.entityId(player.uniqueId)` and calls GeyserUtils `bindEntityId`.
- `CITIZENS_NPC` resolves the stable Citizens registry ID through `CitizensAPI.getNPCRegistry().getById(id)`, verifies the NPC is spawned with a usable Bukkit entity, and calls GeyserUtils `bindEntity`.
- `BUKKIT_ENTITY_UUID` resolves the configured Bukkit entity UUID and calls GeyserUtils `bindEntity`.

Citizens is optional. It is only accessed when `CITIZENS_NPC` is selected, and a missing or disabled Citizens plugin safely cancels that dialogue with one useful warning. For a Citizens NPC with ID `12`, set `Target Type` to `CITIZENS_NPC` and `Citizens NPC ID` to `12`; no entity UUID is needed.

Option criteria are evaluated before opening and rechecked when clicked. The selected visible option is stored as a 1-based `SELECTED_OPTION` context value. Typewriter applies entry and selected-option modifiers and triggers through its normal dialogue lifecycle.

Closing a native form defaults to `CANCEL`; `FINISH` is available per entry. Cancellation avoids reopening forever when Geyser cannot resolve a Java entity ID. Zero usable options finish immediately, matching Typewriter's Java option messenger.

## Upstream GeyserUtils patch

Apply the separate patch at the root of a GeyserUtils checkout:

```powershell
git apply C:\path\to\GeyserUtilsTypewriter\patches\0001-add-java-entity-id-npc-dialogue-binding.patch
git apply C:\path\to\GeyserUtilsTypewriter\patches\0002-restore-npc-request-packet-serializer.patch
mvn -DskipTests -pl spigot -am package
mvn -DskipTests -pl geyser -am package
```

Deploy `spigot/target/geyserutils-spigot-1.0-SNAPSHOT.jar` to the Paper server's `plugins/` directory. Deploy `geyser/target/geyserutils-geyser-1.0-SNAPSHOT.jar` to the `extensions/` directory of the Geyser instance that handles the player. Keep the existing GeyserUtils Velocity bridge in Velocity's `plugins/` directory when using its proxy topology.

Patch `0001` changes only the Spigot `NpcDialogueForm`, the Geyser custom-payload handler, and the native NPC response translator. Patch `0002` replaces only the per-session `NpcRequestPacket` serializer after Geyser selects its processed Bedrock codec. All other illegal-packet protection remains active. The common payload and Velocity bridge are unchanged. The payload continues to carry a Java entity ID; no Bedrock runtime ID crosses the Paper/GeyserUtils plugin-message boundary.

The patched Geyser extension temporarily logs the decoded NPC request fields and confirms entry into `NPCFormResponseTranslator`. These two targeted log lines can be removed after live server confirmation.

## Example graph

```text
Interaction
     ↓
Platform Check
   /              \
Java             Bedrock
 ↓                  ↓
BetterHUD         NPC Dialogue
Spoken               ↓
 ↓                  Continue
Continue             ↓
   \                /
      Next Action
```
