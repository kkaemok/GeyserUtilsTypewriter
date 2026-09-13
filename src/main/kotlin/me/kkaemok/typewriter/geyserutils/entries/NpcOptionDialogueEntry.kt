package me.kkaemok.typewriter.geyserutils.entries

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.EntryTrigger
import com.typewritermc.engine.paper.entry.entries.EventTrigger
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.entries.Var
import me.kkaemok.typewriter.geyserutils.bridge.NpcTarget
import me.kkaemok.typewriter.geyserutils.messenger.GeyserUtilsNpcOptionDialogueMessenger
import org.bukkit.entity.Player
import java.util.Optional
import kotlin.reflect.KClass

@Entry(
    "geyserutils_npc_option_dialogue",
    "Display native Bedrock NPC dialogue options through GeyserUtils",
    "#00AEEF",
    "mdi:format-list-bulleted",
)
@ContextKeys(NpcOptionDialogueContextKeys::class)
class NpcOptionDialogueEntry(
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
    val options: List<NpcDialogueOption> = emptyList(),
    @Placeholder
    @MultiLine
    @Help("Optional raw Bedrock NPC skin/portrait JSON understood by GeyserUtils.")
    val skinData: Optional<Var<String>> = Optional.empty(),
    @Help("What Typewriter should do when the player closes the native dialogue.")
    val closeBehavior: NpcDialogueCloseBehavior = NpcDialogueCloseBehavior.CANCEL,
) : DialogueEntry {
    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<NpcOptionDialogueEntry> =
        GeyserUtilsNpcOptionDialogueMessenger(player, context, this)
}

enum class NpcOptionDialogueContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Int::class)
    SELECTED_OPTION(Int::class),
}

data class NpcDialogueOption(
    @Placeholder
    @Colored
    @Help("Text shown on this native Bedrock NPC button.")
    val text: Var<String> = ConstVar(""),
    @Help("The criteria that must be met for this option to be visible.")
    val criteria: List<Criteria> = emptyList(),
    @Help("The modifiers applied by Typewriter when this option is selected.")
    val modifiers: List<Modifier> = emptyList(),
    @Help("The entries triggered by Typewriter when this option is selected.")
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) {
    val eventTriggers: List<EventTrigger>
        get() = triggers.map(::EntryTrigger)
}
