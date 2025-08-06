/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockPos

object AutoEagle : Module("AutoEagle", Category.PLAYER) {
    private val delay by float("Delay", 50f, 0f..200f)
    private val blocksOnly by boolean("BlocksOnly", true)
    private val directionCheck by boolean("DirectionalCheck", true)

    private var msTimer = MSTimer()
    private var wasOverBlock = false

    override var tag = delay.toString()

    init {
        handler<UpdateEvent> {
        }

        handler<MotionEvent> { event ->
            val thePlayer = mc.thePlayer ?: return@handler
            val theWorld = mc.theWorld ?: return@handler

            if (event.eventState != EventState.PRE)
                return@handler

            if (mc.currentScreen != null)
                return@handler

            val shouldWork = !blocksOnly || 
                (thePlayer.heldItem?.item is ItemBlock && (!directionCheck || thePlayer.moveForward < 0))

            if (shouldWork) {
                val isOverAir = theWorld.getBlockState(BlockPos(
                    thePlayer.posX,
                    thePlayer.posY - 1.0,
                    thePlayer.posZ
                )).block is BlockAir

                if (isOverAir && thePlayer.onGround) {
                    mc.gameSettings.keyBindSneak.pressed = true
                    wasOverBlock = true
                } else if (thePlayer.onGround) {
                    if (wasOverBlock) 
                        msTimer.reset()

                    if (msTimer.hasTimePassed((delay * (Math.random() * 0.1 + 0.95)).toLong())) {
                        mc.gameSettings.keyBindSneak.pressed = false
                    }

                    wasOverBlock = false
                }
            } else {
                mc.gameSettings.keyBindSneak.pressed = false
            }
        }
    }

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = false
    }
}
