package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSword

object DelayRemover : Module("DelayRemover", Category.COMBAT) {

    private val left by boolean("Left", true)
    private val right by boolean("Right", true)
    private val hitReg17 by boolean("1.7HitReg", true)
    private val noPlaceDelay by boolean("NoPlaceDelay", true)

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (left) {
            mc.leftClickCounter = 0
        }

        if (right) {
            mc.rightClickDelayTimer = 0
        }

        if (hitReg17) {
            if (player.isBlocking || player.isUsingItem) {
                if (player.heldItem?.item is ItemSword) {
                    mc.leftClickCounter = 0
                }
            }
        }
        if (noPlaceDelay) {
            if (player.heldItem?.item is ItemBlock) {
                mc.rightClickDelayTimer = 0
                player.itemInUseCount = 0
            }
        }
    }
}