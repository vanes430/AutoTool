package com.github.vanes430.autotool

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class AutoTool : JavaPlugin() {

    private lateinit var nmsVersion: String
    private lateinit var craftBukkitPackageName: String
    private lateinit var craftItemStackClass: Class<*>
    private lateinit var asNMSCopyMethod: Method
    private lateinit var getNMSBlockMethod: Method
    private lateinit var getItemMethod: Method
    private lateinit var getDestroySpeedMethod: Method
    private lateinit var itemClass: Class<*>

    companion object {
        @JvmStatic
        lateinit var instance: AutoTool
            private set
    }

    override fun onEnable() {
        instance = this

        try {
            setupNMS()
        } catch (e: Exception) {
            logger.severe("Gagal menginisialisasi NMS untuk AutoTool. Plugin mungkin tidak kompatibel dengan versi server ini.")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }

        server.pluginManager.registerEvents(ToolListener(this), this)
        getCommand("autotool")?.setExecutor(AutoToolCommand(this))
        saveDefaultConfig()
    }

    private fun setupNMS() {
        nmsVersion = server.javaClass.getPackage().name.substringAfterLast('.')

        craftBukkitPackageName = if (nmsVersion == "craftbukkit") {
            "org.bukkit.craftbukkit"
        } else {
            "org.bukkit.craftbukkit.$nmsVersion"
        }

        setNMSBlockMethod()
        setAsNMSCopyMethod()
        val nmsItemStack = setGetItemMethod()
        setGetDestroySpeedMethod(nmsItemStack)
    }

    fun isAutoToolOn(player: Player): Boolean {
        return !config.getStringList("autotool_disabled").contains(player.uniqueId.toString())
    }

    private fun setNMSBlockMethod() {
        val craftBlockClass = Class.forName("$craftBukkitPackageName.block.CraftBlock")
        getNMSBlockMethod = craftBlockClass.declaredMethods.first {
            it.parameterCount == 0 && (it.name == "getNMS" || it.name == "getNMSBlockData")
        }.apply { isAccessible = true }
    }

    private fun setAsNMSCopyMethod() {
        craftItemStackClass = Class.forName("$craftBukkitPackageName.inventory.CraftItemStack")
        asNMSCopyMethod = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack::class.java)
            .apply { isAccessible = true }
    }

    private fun setGetItemMethod(): Any {
        val nmsItemStack = asNMSCopyMethod.invoke(null, ItemStack(Material.STONE))
        val nmsItemStackClass = nmsItemStack.javaClass
        getItemMethod = nmsItemStackClass.methods.first {
            it.parameterCount == 0 && !it.returnType.isPrimitive && it.returnType.simpleName == "Item"
        }.apply { isAccessible = true }
        return nmsItemStack
    }

    private fun setGetDestroySpeedMethod(nmsItemStack: Any) {
        val nmsItem = getItemMethod.invoke(nmsItemStack)
        itemClass = getSuperClass(nmsItem.javaClass)
        getDestroySpeedMethod = itemClass.methods.first {
            it.returnType == Float::class.javaPrimitiveType &&
                    it.parameterCount == 2 &&
                    it.parameterTypes[0].isAssignableFrom(nmsItemStack.javaClass)
        }.apply { isAccessible = true }
    }

    fun autoTool(player: Player, block: Block) {
        val inventory: PlayerInventory = player.inventory
        var bestSlot = -1
        var highestSpeed = 0.0f

        val currentItem = inventory.itemInHand
        val currentSpeed = getDestroySpeed(currentItem, block)

        for (i in 0..8) {
            val itemStack = inventory.getItem(i) ?: continue

            if (itemStack.type == Material.AIR) continue
            if (block.getDrops(itemStack).isEmpty() && itemStack.type.isBlock) continue

            val destroySpeed = getDestroySpeed(itemStack, block)
            if (destroySpeed > highestSpeed) {
                highestSpeed = destroySpeed
                bestSlot = i
            }
        }

        if (bestSlot != -1 && highestSpeed > currentSpeed && inventory.heldItemSlot != bestSlot) {
            inventory.heldItemSlot = bestSlot
        }
    }

    fun getDestroySpeed(itemStack: ItemStack?, block: Block): Float {
        val stack = itemStack ?: ItemStack(Material.AIR)
        if (block.type == Material.AIR || block.type == Material.BEDROCK) return 0.0f

        return try {
            val nmsItemStack = asNMSCopyMethod.invoke(null, stack)
            val nmsBlockData = getNMSBlockMethod.invoke(block)
            val nmsItem = getItemMethod.invoke(nmsItemStack)
            getDestroySpeedMethod.invoke(nmsItem, nmsItemStack, nmsBlockData) as Float
        } catch (e: Exception) {
            1.0f
        }
    }

    private fun getSuperClass(clazz: Class<*>): Class<*> {
        var superClass = clazz
        while (superClass.superclass != null && superClass.superclass != Object::class.java) {
            if (Modifier.isAbstract(superClass.superclass.modifiers)) {
                break
            }
            superClass = superClass.superclass
        }
        return superClass
    }
}