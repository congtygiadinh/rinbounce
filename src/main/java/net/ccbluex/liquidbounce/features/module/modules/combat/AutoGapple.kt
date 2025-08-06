/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
 package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.init.Items
import net.minecraft.item.ItemAppleGold
import net.minecraft.potion.Potion

object AutoGapple : Module("AutoGapple", Category.COMBAT) {

    private val healthThreshold by float("Health", 14f, 1f..20f)
    private val delayMs by int("Delay", 1000, 0..5000)
    private val eatOnAir by boolean("EatOnAir", false)
    private val waitForEffect by boolean("WaitEffect", true)

    private val delayTimer = MSTimer()
    private var waitingEffect = false
    private var prevSlot = -1

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (!eatOnAir && !player.onGround) return@handler

        if (waitingEffect) {
            if (hasGappleEffects(player)) {
                waitingEffect = false
                delayTimer.reset()

                if (prevSlot != -1 && prevSlot in 0..8) {
                    player.inventory.currentItem = prevSlot
                    mc.playerController.updateController()
                    prevSlot = -1
                }
            }
            return@handler
        }

        if (player.health >= healthThreshold) return@handler
        if (!delayTimer.hasTimePassed(delayMs)) return@handler

        val gappleSlot = findGappleSlot() ?: return@handler
        prevSlot = player.inventory.currentItem

        if (player.inventory.currentItem != gappleSlot) {
            player.inventory.currentItem = gappleSlot
            mc.playerController.updateController()
        }
        if (!player.isEating) {
            mc.gameSettings.keyBindUseItem.pressed = true

            if (waitForEffect) {
                waitingEffect = true
            } else {
                delayTimer.reset()

                if (prevSlot != -1 && prevSlot in 0..8) {
                    player.inventory.currentItem = prevSlot
                    mc.playerController.updateController()
                    prevSlot = -1
                }
            }
        }
    }

    override fun onDisable() {
        mc.gameSettings.keyBindUseItem.pressed = false
        waitingEffect = false

        if (prevSlot != -1 && prevSlot in 0..8) {
            mc.thePlayer?.inventory?.currentItem = prevSlot
            mc.playerController.updateController()
            prevSlot = -1
        }
    }

    private fun findGappleSlot(): Int? {
        for (i in 0..8) {
            val stack = mc.thePlayer?.inventory?.getStackInSlot(i) ?: continue
            if (stack.item is ItemAppleGold) return i
        }
        return null
    }

    private fun hasGappleEffects(player: net.minecraft.entity.player.EntityPlayer): Boolean {
        return player.isPotionActive(Potion.regeneration) || player.isPotionActive(Potion.absorption)
    }

    override val tag get() = "${healthThreshold.toInt()}"
}