package com.github.vanes430.autotool

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AutoToolCommand(private val plugin: AutoTool) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            if (sender.hasPermission("autotool.reload")) {
                plugin.reloadConfig()
                sender.sendMessage("${ChatColor.GREEN}AutoTool reloaded!")
            } else {
                sender.sendMessage("${ChatColor.RED}You do not have permission.")
            }
            return true
        }

        if (sender is Player) {
            val disabledList = plugin.config.getStringList("autotool_disabled").toMutableList()

            if (plugin.isAutoToolOn(sender)) {
                disabledList.add(sender.uniqueId.toString())
                val message = plugin.config.getString("messages.autotool_off", "&cAutoTool has been disabled.")
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message!!))
            } else {
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