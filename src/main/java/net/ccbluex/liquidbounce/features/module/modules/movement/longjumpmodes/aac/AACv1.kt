/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils

object AACv1 : LongJumpMode("AACv1") {
    override fun onUpdate() {
        mc.thePlayer.motionY += 0.05999
        MovementUtils.speed *= 1.08f
    }
}