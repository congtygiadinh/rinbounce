package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode

object MatrixNew : SpeedMode("MatrixNew") {
    override fun onDisable() {
        mc.thePlayer.jumpMovementFactor = 0.02F
    }
}