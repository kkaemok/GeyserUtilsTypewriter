package me.kkaemok.typewriter.geyserutils.entries

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.utils.isFloodgate

@Entry(
    "platform_check",
    "Continue through the Java or Bedrock branch for this player",
    "#7C4DFF",
    "mdi:call-split",
)
class PlatformCheckEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    @Help("Entries to continue to for a Java Edition player.")
    val javaTriggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Entries to continue to for a Floodgate/Bedrock Edition player.")
    val bedrockTriggers: List<Ref<TriggerableEntry>> = emptyList(),
) : ActionEntry {
    override val triggers: List<Ref<TriggerableEntry>>
        get() = emptyList()

    override fun ActionTrigger.execute() {
        disableAutomaticTriggering()
        applyModifiers()

        val selectedBranch = if (player.isFloodgate) bedrockTriggers else javaTriggers
        selectedBranch.triggerFor(player)
    }
}
