/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.LinkedList

object MemoryFix : Module("MemoryFix", Category.MISC) {

    private val gcSteps = LinkedList<() -> Unit>()
    private var tickCounter = 0
    private val intervalTicks = 20

    override fun onEnable() {
        tickCounter = 0
        gcSteps.clear()

        gcSteps.add { cleanInternalCaches() }
        gcSteps.add { System.runFinalization() }
        gcSteps.add { System.gc() }

        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this)
        gcSteps.clear()
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (!state) return

        if (++tickCounter >= intervalTicks && gcSteps.isNotEmpty()) {
            tickCounter = 0
            try {
                gcSteps.poll()?.invoke()
            } catch (_: Exception) {}
        }
    }
                                                                                                                                                                                      // credit : beophiman
                                                                                                                                                                                     // chuc may e may yeu chs game vui ve
    private fun cleanInternalCaches() {
        LiquidBounce.fileManager?.let {
            try {
                val method = it::class.java.getDeclaredMethod("unloadUnused")
                method.isAccessible = true
                method.invoke(it)
            } catch (_: Exception) {}
        }
    }
}
