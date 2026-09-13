package me.kkaemok.typewriter.geyserutils.bridge

import net.citizensnpcs.api.CitizensAPI
import org.bukkit.entity.Player

/** Loaded only after the Citizens plugin has been confirmed present and enabled. */
internal object CitizensNpcAdapter {
    fun resolve(player: Player, npcId: Int): Result<ResolvedNpcTarget> {
        val integrationAvailable = CitizensAPI.hasImplementation()
        if (!integrationAvailable) {
            return failure(citizensNpcValidationMessage(npcId, CitizensNpcStatus(integrationAvailable = false))!!)
        }

        if (npcId < 0) {
            return failure(citizensNpcValidationMessage(npcId, CitizensNpcStatus())!!)
        }

        val npc = CitizensAPI.getNPCRegistry().getById(npcId)
        val entity = npc?.entity
        val status = CitizensNpcStatus(
            exists = npc != null,
            spawned = npc?.isSpawned == true,
            entityPresent = entity != null,
            entityUsable = entity?.let {
                it.isValid && !it.isDead && it.entityId > 0 && it.world.uid == player.world.uid
            } == true,
        )
        citizensNpcValidationMessage(npcId, status)?.let { return failure(it) }
        return Result.success(ResolvedNpcTarget.BukkitEntity(entity!!))
    }

    private fun failure(message: String): Result<ResolvedNpcTarget> =
        Result.failure(IllegalStateException(message))
}
