package me.kkaemok.typewriter.geyserutils.bridge

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.entity.AudienceEntityDisplay
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntityInstanceEntry
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.findDisplay
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.Optional
import java.util.UUID

data class NpcTarget(
    @Help("Select exactly which NPC target system this dialogue uses.")
    val targetType: NpcTargetType = NpcTargetType.TYPEWRITER_ENTITY,
    @Help("Typewriter entity used only when Target Type is TYPEWRITER_ENTITY.")
    val typewriterNpc: Ref<EntityInstanceEntry> = emptyRef(),
    @Help("Stable Citizens registry ID used only when Target Type is CITIZENS_NPC.")
    val citizensNpcId: Int = 0,
    @Placeholder
    @Help("Bukkit entity UUID used only when Target Type is BUKKIT_ENTITY_UUID.")
    val fallbackBukkitEntityUuid: Optional<Var<String>> = Optional.empty(),
)

enum class NpcTargetType {
    TYPEWRITER_ENTITY,
    CITIZENS_NPC,
    BUKKIT_ENTITY_UUID,
}

sealed interface ResolvedNpcTarget {
    val javaEntityId: Int

    data class TypewriterEntity(override val javaEntityId: Int) : ResolvedNpcTarget

    data class BukkitEntity(val entity: Entity) : ResolvedNpcTarget {
        override val javaEntityId: Int
            get() = entity.entityId
    }
}

fun interface NpcTargetResolver {
    fun resolve(player: Player, context: InteractionContext, target: NpcTarget): Result<ResolvedNpcTarget>
}

object DefaultNpcTargetResolver : NpcTargetResolver {
    private const val CITIZENS_PLUGIN = "Citizens"

    override fun resolve(
        player: Player,
        context: InteractionContext,
        target: NpcTarget,
    ): Result<ResolvedNpcTarget> = selectNpcTargetResolver(
        target.targetType,
        typewriter = { resolveTypewriter(player, target) },
        citizens = { resolveCitizens(player, target.citizensNpcId) },
        bukkit = { resolveBukkit(player, context, target) },
    )

    private fun resolveTypewriter(player: Player, target: NpcTarget): Result<ResolvedNpcTarget> {
        if (!target.typewriterNpc.isSet) {
            return failure("No Typewriter entity is configured")
        }
        val instance = target.typewriterNpc.get()
            ?: return failure("Typewriter entity '${target.typewriterNpc.id}' does not exist")
        val display = target.typewriterNpc.findDisplay<AudienceEntityDisplay>()
            ?: return failure("Typewriter entity '${instance.name}' target is not available for this player")

        if (!display.canView(player.uniqueId) || !display.isSpawnedIn(player.uniqueId)) {
            return failure("Typewriter entity '${instance.name}' target is not available for ${player.name}")
        }

        val entityId = display.entityId(player.uniqueId)
        if (entityId <= 0 || !display.playerSeesEntity(player.uniqueId, entityId)) {
            return failure("Typewriter entity '${instance.name}' has no valid visible Java entity ID for ${player.name}")
        }
        return Result.success(ResolvedNpcTarget.TypewriterEntity(entityId))
    }

    private fun resolveCitizens(player: Player, npcId: Int): Result<ResolvedNpcTarget> {
        val plugin = Bukkit.getPluginManager().getPlugin(CITIZENS_PLUGIN)
            ?: return failure("Citizens integration is unavailable because Citizens is not installed")
        if (!plugin.isEnabled) {
            return failure("Citizens integration is unavailable because Citizens is not enabled")
        }

        return try {
            CitizensNpcAdapter.resolve(player, npcId)
        } catch (error: LinkageError) {
            failure("Citizens integration is unavailable because its API could not be loaded: ${error.message}")
        } catch (error: Exception) {
            failure("Citizens NPC $npcId could not be resolved: ${error.message}")
        }
    }

    private fun resolveBukkit(
        player: Player,
        context: InteractionContext,
        target: NpcTarget,
    ): Result<ResolvedNpcTarget> {
        val fallback = target.fallbackBukkitEntityUuid.orElse(null)
            ?: return failure("No Bukkit entity UUID is configured")
        val rawUuid = fallback.get(player, context).parsePlaceholders(player).trim()
        if (rawUuid.isEmpty()) return failure("No Bukkit entity UUID is configured")
        val uuid = runCatching { UUID.fromString(rawUuid) }.getOrElse {
            return failure("Bukkit entity UUID '$rawUuid' is invalid")
        }
        val entity = Bukkit.getEntity(uuid)
            ?: return failure("Bukkit entity UUID '$uuid' could not be resolved")

        if (!entity.isValid || entity.isDead || entity.world.uid != player.world.uid) {
            return failure("Bukkit entity UUID '$uuid' is not valid and usable in ${player.name}'s world")
        }
        return Result.success(ResolvedNpcTarget.BukkitEntity(entity))
    }

    private fun failure(message: String): Result<ResolvedNpcTarget> = Result.failure(IllegalStateException(message))
}
