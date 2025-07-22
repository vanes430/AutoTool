package com.github.vanes430.autotool

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType

class ToolListener(private val plugin: AutoTool) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerAnimate(event: PlayerAnimationEvent) {
        if (event.animationType != PlayerAnimationType.ARM_SWING) return

        val player = event.player
        if (!plugin.isAutoToolOn(player)) return

        // Dapatkan blok yang ditarget oleh pemain
        val targetBlock = player.getTargetBlock(null as? Set<Material>, 5) ?: return

        if (targetBlock.type == Material.AIR) return

        plugin.autoTool(player, targetBlock)
    }
}