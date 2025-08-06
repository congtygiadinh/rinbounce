/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer

object Grim19 : NoFallMode("Grim 1.9") {
    override fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (event.eventState == EventState.PRE && thePlayer.fallDistance > NoFall.minFallDistance) {
            if (thePlayer.onGround) {
                sendPacket(C03PacketPlayer.C04PacketPlayerPosition(thePlayer.posX, thePlayer.posY + 0.0000001, thePlayer.posZ, false))
                thePlayer.fallDistance = 0f
            }
        }
    }
}
