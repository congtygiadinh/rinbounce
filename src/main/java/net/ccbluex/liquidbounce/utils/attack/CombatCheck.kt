/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.utils.attack

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

object CombatCheck {

    private val lastAttackTimer = MSTimer()
    private var target: EntityLivingBase? = null
    var inCombat = false
        private set

    fun isPlayerInCombat(player: EntityPlayer): Boolean {
        return inCombat
    }

    fun setTarget(entity: EntityLivingBase?) {
        target = entity
        if (entity != null) {
            lastAttackTimer.reset()
            inCombat = true
        }
    }

    fun updateCombatState() {
        if (target != null && MinecraftInstance.mc.thePlayer != null) {
            if (target!!.hurtTime > 0) {
                lastAttackTimer.reset()
                inCombat = true
                return
            }

            if (!lastAttackTimer.hasTimePassed(250)) {
                inCombat = true
                return
            }

            if (MinecraftInstance.mc.thePlayer.getDistanceToEntity(target) > 7 || target!!.isDead) {
                target = null
                inCombat = false
            } else {
                inCombat = true
            }
        } else {
            inCombat = false
        }
    }
}
