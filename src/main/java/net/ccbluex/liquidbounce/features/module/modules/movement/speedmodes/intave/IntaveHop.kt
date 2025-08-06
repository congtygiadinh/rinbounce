package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.minecraft.util.MathHelper
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.random.Random

object IntaveHop : SpeedMode("IntaveHop") { // skidded by duyundz
    override fun onUpdate() {
        val thePlayer = mc.thePlayer ?: return

        if (!thePlayer.isMoving || thePlayer.isInLiquid || thePlayer.isOnLadder || thePlayer.isRiding)
            return
        if (thePlayer.movementInput.moveForward <= 0) {
            mc.timer.timerSpeed = 1.0f
            thePlayer.speedInAir = 0.02f
            return
        }

        val moveYaw = Math.toDegrees(atan2(thePlayer.motionZ, thePlayer.motionX)).toFloat() - 90f
        val yawDiff = abs(MathHelper.wrapAngleTo180_float(thePlayer.rotationYaw - moveYaw))
        val deltaY = abs(thePlayer.posY - thePlayer.prevPosY)

        if (yawDiff > 10f || deltaY > 0.0015f) {
            mc.timer.timerSpeed = 1.0f
            thePlayer.speedInAir = 0.02f
            return
        }

        if (thePlayer.onGround) {
            thePlayer.tryJump()
            mc.timer.timerSpeed = 0.9385f
            thePlayer.speedInAir = 0.0201f
        }

        if (thePlayer.fallDistance > 0.7f && thePlayer.fallDistance <= 1.0f) {
            val increaseFactor = if (Random.nextBoolean()) {
                1.1f + Random.nextFloat() * (1.3f - 1.1f)
            } else {
                1.0f + Random.nextFloat() * (1.2f - 1.0f)
            }

            if (thePlayer.ticksExisted % 3 == 0) {
                mc.timer.timerSpeed = 1.925f * increaseFactor
            } else if (thePlayer.fallDistance < 1.25f) {
                mc.timer.timerSpeed = 1.7975f * increaseFactor
            }
        }
    }
}