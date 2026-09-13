package me.kkaemok.typewriter.geyserutils.entries

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.entries.Var
import me.kkaemok.typewriter.geyserutils.bridge.NpcTarget
import me.kkaemok.typewriter.geyserutils.messenger.GeyserUtilsNpcDialogueMessenger
import org.bukkit.entity.Player
import java.util.Optional

@Entry(
    "geyserutils_npc_dialogue",
    "Display native Bedrock NPC dialogue through GeyserUtils",
    "#00AEEF",
    "mdi:message-text",
)
class NpcDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val speaker: Ref<SpeakerEntry> = emptyRef(),
    @Placeholder
    @Colored
    @MultiLine
    val text: Var<String> = ConstVar(""),
    @Help("Select a Typewriter entity, Citizens NPC, or Bukkit entity target for the native dialogue.")
    val target: NpcTarget = NpcTarget(),
    @Placeholder
    @Colored
    @Default("\"Continue\"")
    @Help("Text shown on the native continue button.")
    val continueText: Var<String> = ConstVar("Continue"),
    @Placeholder
    @MultiLine
    @Help("Optional raw Bedrock NPC skin/portrait JSON understood by GeyserUtils.")
    val skinData: Optional<Var<String>> = Optional.empty(),
    @Help("What Typewriter should do when the player closes the native dialogue.")
    val closeBehavior: NpcDialogueCloseBehavior = NpcDialogueCloseBehavior.CANCEL,
) : DialogueEntry {
    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<NpcDialogueEntry> =
        GeyserUtilsNpcDialogueMessenger(player, context, this)
}

enum class NpcDialogueCloseBehavior {
    CANCEL,
    FINISH,
}
