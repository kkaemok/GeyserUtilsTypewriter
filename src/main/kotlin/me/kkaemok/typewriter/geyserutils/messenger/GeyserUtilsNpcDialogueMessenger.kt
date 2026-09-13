package me.kkaemok.typewriter.geyserutils.messenger

import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.dialogue.TickContext
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.utils.isFloodgate
import com.typewritermc.engine.paper.utils.stripped
import me.kkaemok.typewriter.geyserutils.bridge.DefaultNpcTargetResolver
import me.kkaemok.typewriter.geyserutils.bridge.GeyserUtilsBridge
import me.kkaemok.typewriter.geyserutils.entries.NpcDialogueCloseBehavior
import me.kkaemok.typewriter.geyserutils.entries.NpcDialogueEntry
import org.bukkit.entity.Player

class GeyserUtilsNpcDialogueMessenger(
    player: Player,
    context: InteractionContext,
    entry: NpcDialogueEntry,
) : DialogueMessenger<NpcDialogueEntry>(player, context, entry) {
    private var boundEntityId: Int? = null
    private var formOpen = false
    private var disposed = false

    override fun init() {
        super.init()
        if (!player.isFloodgate) {
            cancelWithWarning("NPC Dialogue '${entry.name}' was reached by Java player ${player.name}")
            return
        }

        val target = DefaultNpcTargetResolver.resolve(player, context, entry.target)
            .getOrElse {
                cancelWithWarning("Cannot open NPC Dialogue '${entry.name}' for ${player.name}: ${it.message}")
                return
            }
        boundEntityId = target.javaEntityId

        val result = GeyserUtilsBridge.open(
            player = player,
            target = target,
            title = entry.speakerDisplayName.get(player, context).parsePlaceholders(player).stripped(),
            dialogue = entry.text.get(player, context).parsePlaceholders(player).stripped(),
            skinData = entry.skinData.orElse(null)?.get(player, context)?.parsePlaceholders(player),
            buttons = listOf(entry.continueText.get(player, context).parsePlaceholders(player).stripped()),
            onButton = ::handleButton,
            onClose = ::handleClose,
        )
        result.onSuccess { formOpen = true }
            .onFailure { cancelWithWarning("Cannot send NPC Dialogue '${entry.name}' to ${player.name}: ${it.message}") }
    }

    override fun tick(context: TickContext) {
        if (state != MessengerState.RUNNING) return
        if (!player.isOnline) {
            formOpen = false
            state = MessengerState.CANCELLED
            return
        }

        val currentEntityId = DefaultNpcTargetResolver.resolve(player, this.context, entry.target)
            .getOrNull()?.javaEntityId
        if (currentEntityId == null || currentEntityId != boundEntityId) {
            cancelWithWarning("NPC target for '${entry.name}' disappeared or changed for ${player.name}")
            closeNativeForm()
        }
    }

    private fun handleButton(index: Int) {
        if (disposed || state != MessengerState.RUNNING) return
        formOpen = false
        state = if (index == 0) MessengerState.FINISHED else MessengerState.CANCELLED
        if (index != 0) {
            logger.warning("GeyserUtils returned invalid button index $index for NPC Dialogue '${entry.name}'")
        }
    }

    private fun handleClose() {
        if (disposed || state != MessengerState.RUNNING) return
        formOpen = false
        state = when (entry.closeBehavior) {
            NpcDialogueCloseBehavior.CANCEL -> MessengerState.CANCELLED
            NpcDialogueCloseBehavior.FINISH -> MessengerState.FINISHED
        }
    }

    private fun cancelWithWarning(message: String) {
        logger.warning(message)
        state = MessengerState.CANCELLED
    }

    private fun closeNativeForm() {
        if (!formOpen) return
        formOpen = false
        GeyserUtilsBridge.close(player).onFailure {
            logger.warning("Could not close GeyserUtils NPC dialogue for ${player.name}: ${it.message}")
        }
    }

    override fun end() = Unit

    override fun dispose() {
        disposed = true
        closeNativeForm()
        super.dispose()
    }
}
