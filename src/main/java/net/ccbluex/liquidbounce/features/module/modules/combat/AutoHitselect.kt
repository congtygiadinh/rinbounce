/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.MovementInputEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.config.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.MovementInput
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import kotlin.math.abs

/**
 * Automatically performs hit selection to initiate combos
 */
object AutoHitselect : Module("AutoHitselect", Category.COMBAT) {
    private val MODES = arrayOf("Pause", "Active")
    private val PREFERENCES = arrayOf("Move speed", "KB reduction", "Critical hits")
    private val mode by choices("Mode", MODES, "Pause")
    private val preference by choices("Preference", PREFERENCES, "Move speed")
    private val delay by int("Delay", 420, 300..500)
    private val chance by int("Chance", 80, 0..100)
    private val rangeValue by float("Range", 8f, 1f..20f)

    private var attackTime = -1L
    private var currentShouldAttack = false

    val onGameTick = handler<GameTickEvent> {
        val thePlayer = mc.thePlayer ?: return@handler
        val target = getNearestEntityInRange() ?: run {
            resetState()
            return@handler
        }

        currentShouldAttack = false
        if (Math.random() * 100 > chance) {
            currentShouldAttack = true
        } else {
            when (preference) {
                "KB reduction" -> currentShouldAttack = !thePlayer.onGround && thePlayer.motionY < 0
                "Critical hits" -> currentShouldAttack = thePlayer.hurtTime > 0 && !thePlayer.onGround && thePlayer.isMoving
            }
            if (!currentShouldAttack)
                currentShouldAttack = System.currentTimeMillis() - attackTime >= delay
        }
    }

    val onMoveInput = handler<MovementInputEvent> { event ->
    }

    private fun resetState() {
        currentShouldAttack = false
    }

    private fun getNearestEntityInRange(): EntityLivingBase? {
        val thePlayer = mc.thePlayer ?: return null
        val entities = mc.theWorld.loadedEntityList ?: return null
        return entities.asSequence()
            .filterIsInstance<EntityLivingBase>()
            .filter { isSelected(it, true) && mc.thePlayer.getDistanceToEntityBox(it) <= rangeValue }
            .minByOrNull { mc.thePlayer.getDistanceToEntityBox(it) }
    }

    fun canAttack(): Boolean = canSwing()
    fun canSwing(): Boolean {
        if (!this.state || mode == "Active") return true
        return currentShouldAttack
    }

    override val tag
        get() = mode
}
