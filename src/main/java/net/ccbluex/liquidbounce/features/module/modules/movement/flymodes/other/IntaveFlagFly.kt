package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S27PacketExplosion
import net.ccbluex.liquidbounce.event.PacketEvent

object IntaveFlagFly : FlyMode("IntaveFlagFly") { // skidded by duyundz
    private var boosting = false
    private var boostTicks = 0

    override fun onEnable() {
        boosting = false
        boostTicks = 0
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is S27PacketExplosion) {
            boosting = true
            boostTicks = 0
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (boosting) {
            val boostValue = 2.2 + Math.random() * 0.25 // Có thể điều chỉnh để bay xa hơn/nhiều hơn
            val yawRad = Math.toRadians(player.rotationYaw.toDouble())
            player.motionX = -Math.sin(yawRad) * boostValue + ((Math.random() - 0.5) * 0.07)
            player.motionZ =  Math.cos(yawRad) * boostValue + ((Math.random() - 0.5) * 0.07)
            player.motionY = 0.42 + (Math.random() - 0.5) * 0.08
            player.fallDistance = 0f
            boostTicks++
            if (boostTicks >= Fly.maxFlyTicksValue) {
                boosting = false
                player.motionX = 0.0
                player.motionZ = 0.0
            }
            return
        }
    }

    override fun onDisable() {
        boosting = false
        boostTicks = 0
    }
}