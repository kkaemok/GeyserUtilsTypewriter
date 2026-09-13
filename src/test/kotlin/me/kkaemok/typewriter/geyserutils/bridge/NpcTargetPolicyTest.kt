package me.kkaemok.typewriter.geyserutils.bridge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NpcTargetPolicyTest {
    @Test
    fun `Typewriter packet NPC selects Java entity ID resolver`() {
        val result = selectNpcTargetResolver(
            NpcTargetType.TYPEWRITER_ENTITY,
            typewriter = { "bindEntityId:42" },
            citizens = { error("Citizens resolver must not run") },
            bukkit = { error("Bukkit resolver must not run") },
        )
        assertEquals("bindEntityId:42", result)
    }

    @Test
    fun `valid spawned Citizens NPC selects Bukkit entity resolver`() {
        assertNull(citizensNpcValidationMessage(12, CitizensNpcStatus()))
        val result = selectNpcTargetResolver(
            NpcTargetType.CITIZENS_NPC,
            typewriter = { error("Typewriter resolver must not run") },
            citizens = { "bindEntity:12" },
            bukkit = { error("Bukkit UUID resolver must not run") },
        )
        assertEquals("bindEntity:12", result)
    }

    @Test
    fun `invalid Citizens NPC ID is rejected`() {
        assertEquals(
            "Citizens NPC ID -1 is invalid",
            citizensNpcValidationMessage(-1, CitizensNpcStatus()),
        )
        assertEquals(
            "Citizens NPC 12 was not found",
            citizensNpcValidationMessage(12, CitizensNpcStatus(exists = false)),
        )
    }

    @Test
    fun `despawned Citizens NPC is rejected`() {
        assertEquals(
            "Citizens NPC 12 is not currently spawned",
            citizensNpcValidationMessage(12, CitizensNpcStatus(spawned = false)),
        )
    }

    @Test
    fun `missing Citizens plugin is rejected`() {
        assertEquals(
            "Citizens integration is unavailable because Citizens is not installed",
            citizensNpcValidationMessage(12, CitizensNpcStatus(integrationAvailable = false)),
        )
    }

    @Test
    fun `Bukkit UUID mode selects Bukkit resolver`() {
        val result = selectNpcTargetResolver(
            NpcTargetType.BUKKIT_ENTITY_UUID,
            typewriter = { error("Typewriter resolver must not run") },
            citizens = { error("Citizens resolver must not run") },
            bukkit = { "bindEntity:uuid" },
        )
        assertEquals("bindEntity:uuid", result)
    }

    @Test
    fun `no configured target returns selected mode error`() {
        val result = selectNpcTargetResolver(
            NpcTargetType.TYPEWRITER_ENTITY,
            typewriter = { Result.failure<String>(IllegalStateException("No Typewriter entity is configured")) },
            citizens = { error("Citizens resolver must not run") },
            bukkit = { error("Bukkit resolver must not run") },
        )
        assertEquals("No Typewriter entity is configured", result.exceptionOrNull()?.message)
    }
}
