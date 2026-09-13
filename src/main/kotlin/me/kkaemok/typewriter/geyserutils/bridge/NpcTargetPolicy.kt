package me.kkaemok.typewriter.geyserutils.bridge

internal inline fun <T> selectNpcTargetResolver(
    targetType: NpcTargetType,
    typewriter: () -> T,
    citizens: () -> T,
    bukkit: () -> T,
): T = when (targetType) {
    NpcTargetType.TYPEWRITER_ENTITY -> typewriter()
    NpcTargetType.CITIZENS_NPC -> citizens()
    NpcTargetType.BUKKIT_ENTITY_UUID -> bukkit()
}

internal data class CitizensNpcStatus(
    val integrationAvailable: Boolean = true,
    val exists: Boolean = true,
    val spawned: Boolean = true,
    val entityPresent: Boolean = true,
    val entityUsable: Boolean = true,
)

internal fun citizensNpcValidationMessage(npcId: Int, status: CitizensNpcStatus): String? = when {
    !status.integrationAvailable -> "Citizens integration is unavailable because Citizens is not installed"
    npcId < 0 -> "Citizens NPC ID $npcId is invalid"
    !status.exists -> "Citizens NPC $npcId was not found"
    !status.spawned -> "Citizens NPC $npcId is not currently spawned"
    !status.entityPresent -> "Citizens NPC $npcId has no Bukkit entity"
    !status.entityUsable -> "Citizens NPC $npcId does not have a valid usable Bukkit entity in the player's world"
    else -> null
}
