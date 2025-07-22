package com.github.vanes430.autotool

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import java.util.*

class ToolListener(private val plugin: AutoTool) : Listener {
    private val recentRightClick = mutableMapOf<UUID, Long>()
    private val lastSwitch = mutableMapOf<UUID, Long>()

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            recentRightClick[event.player.uniqueId] = System.currentTimeMillis()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerAnimate(event: PlayerAnimationEvent) {
        if (event.animationType != PlayerAnimationType.ARM_SWING) return

        val player = event.player
        if (player.isSneaking) return
        if (!plugin.isAutoToolOn(player)) return

        // Cek, apakah player baru saja right click?
        val lastRightClick = recentRightClick[player.uniqueId] ?: 0
        if (System.currentTimeMillis() - lastRightClick < 150) return

        // Cooldown biar gak spam
        val now = System.currentTimeMillis()
        val last = lastSwitch[player.uniqueId] ?: 0
        if (now - last < 200) return
        lastSwitch[player.uniqueId] = now

        val targetBlock = player.getTargetBlockExact(5) ?: return
        if (targetBlock.type == Material.AIR) return

        plugin.autoTool(player, targetBlock)
    }
}
