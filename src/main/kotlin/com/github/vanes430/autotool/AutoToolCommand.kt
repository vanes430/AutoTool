package com.github.vanes430.autotool

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AutoToolCommand(private val plugin: AutoTool) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        // Handle reload command
        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            if (sender.hasPermission("autotool.reload")) {
                plugin.reloadConfig()
                sender.sendMessage("${ChatColor.GREEN}AutoTool reloaded!")
            } else {
                sender.sendMessage("${ChatColor.RED}You do not have permission.")
            }
            return true
        }

        // Handle toggle command for players
        if (sender is Player) {
            val disabledList = plugin.config.getStringList("autotool_disabled").toMutableList()

            if (plugin.isAutoToolOn(sender)) {
                // Turn it off
                disabledList.add(sender.uniqueId.toString())
                val message = plugin.config.getString("messages.autotool_off", "&cAutoTool has been disabled.")
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message!!))
            } else {
                // Turn it on
                disabledList.remove(sender.uniqueId.toString())
                val message = plugin.config.getString("messages.autotool_on", "&aAutoTool has been enabled.")
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message!!))
            }

            plugin.config.set("autotool_disabled", disabledList)
            plugin.saveConfig()
        } else {
            sender.sendMessage("This command can only be run by a player.")
        }
        return true
    }
}