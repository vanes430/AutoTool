package com.github.vanes430.autotool

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action

class ToolListener(private val plugin: AutoTool) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK) return

        val player = event.player
        if (player.isSneaking) return // Jangan auto tool saat sneak
        if (!plugin.isAutoToolOn(player)) return

        val block = event.clickedBlock ?: return
        if (block.type == Material.AIR) return

        plugin.autoTool(player, block)
    }
}
