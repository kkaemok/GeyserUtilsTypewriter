package me.kkaemok.typewriter.geyserutils.bridge

import me.zimzaza4.geyserutils.common.form.element.NpcDialogueButton
import me.zimzaza4.geyserutils.spigot.api.form.NpcDialogueForm
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.geysermc.floodgate.api.FloodgateApi

object GeyserUtilsBridge {
    private const val GEYSER_UTILS_PLUGIN = "GeyserUtils"
    private const val FLOODGATE_PLUGIN = "floodgate"

    fun open(
        player: Player,
        target: ResolvedNpcTarget,
        title: String,
        dialogue: String,
        skinData: String?,
        buttons: List<String>,
        onButton: (Int) -> Unit,
        onClose: () -> Unit,
    ): Result<Unit> = runCatching {
        check(Bukkit.getPluginManager().isPluginEnabled(GEYSER_UTILS_PLUGIN)) {
            "$GEYSER_UTILS_PLUGIN is not enabled"
        }
        check(Bukkit.getPluginManager().isPluginEnabled(FLOODGATE_PLUGIN)) {
            "$FLOODGATE_PLUGIN is not enabled"
        }
        require(target.javaEntityId > 0) { "Java entity ID must be positive" }

        val floodgatePlayer = FloodgateApi.getInstance().getPlayer(player.uniqueId)
            ?: error("${player.name} is not registered as a Floodgate player")
        val nativeButtons = java.util.ArrayList<NpcDialogueButton>(buttons.size)
        buttons.forEach { label ->
            nativeButtons += NpcDialogueButton()
                .text(label)
                .commands(java.util.ArrayList<String>())
                .mode(NpcDialogueButton.ButtonMode.BUTTON_MODE)
                .hasNextForm(false)
        }

        val form = NpcDialogueForm()
            .title(title)
            .dialogue(dialogue)
            .buttons(nativeButtons)
            .handler { _, index -> onButton(index) }
            .closeHandler { onClose() }

        when (target) {
            is ResolvedNpcTarget.TypewriterEntity -> form.bindEntityId(target.javaEntityId)
            is ResolvedNpcTarget.BukkitEntity -> form.bindEntity(target.entity)
        }

        if (!skinData.isNullOrBlank()) {
            form.skinData(skinData)
        }
        form.send(floodgatePlayer)
    }

    fun close(player: Player): Result<Unit> = runCatching {
        if (!Bukkit.getPluginManager().isPluginEnabled(GEYSER_UTILS_PLUGIN)) return@runCatching
        if (!Bukkit.getPluginManager().isPluginEnabled(FLOODGATE_PLUGIN)) return@runCatching
        val floodgatePlayer = FloodgateApi.getInstance().getPlayer(player.uniqueId) ?: return@runCatching
        NpcDialogueForm.closeForm(floodgatePlayer)
    }
}
