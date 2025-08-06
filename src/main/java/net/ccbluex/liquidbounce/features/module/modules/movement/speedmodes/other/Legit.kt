/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.minecraft.client.entity.EntityPlayerSP

object Legit : SpeedMode("Legit") {
    private var isJumping = false 

    override fun onStrafe() {
        val player = mc.thePlayer ?: return

        if (player.onGround && player.isMoving && !isJumping) {
            try {
                isJumping = true 
                player.jump()
            } finally {
                isJumping = false 
            }
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        player.isSprinting = player.movementInput.moveForward > 0.8
    }
}