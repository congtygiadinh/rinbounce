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
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.config.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemSword
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.MovementInput
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import kotlin.math.*

/**
 * Helps with combat by providing various utilities like combo breaking, combo keeping, and smart blocking
 */
object CombatHelper : Module("CombatHelper", Category.COMBAT) {
    private val publicSearchRange by float("PublicSearchRange", 6f, 0.1f..12f)

    private val comboBreaker by boolean("ComboBreaker", true)
    private val breakerAttackRange by float("BreakerAttackRange", 3f, 0.1f..8f) { comboBreaker }

    private val keepCombo by boolean("KeepCombo", true)
    private val keepComboAttackRange by float("KeepComboAttackRange", 3f, 0.1f..8f) { keepCombo }

    private val smartBlocking by boolean("SmartBlocking", true)
    private val blockRange by float("BlockRange", 2f, 0.1f..8f) { smartBlocking }

    private val adaptiveStrafe by boolean("AdaptiveStrafe", false)
    private val forceStrafe by boolean("ForceStrafe", false) { adaptiveStrafe }
    private val strafeDistance by float("TargetStrafeDistance", 3f, 2.5f..6f) { adaptiveStrafe }
    private val maxStrafeDistance by float("MaxStrafeDistance", 8f, 4.5f..15f) { adaptiveStrafe }
    private val predictionTicks by int("TargetPredictionTicks", 5, 1..20) { adaptiveStrafe }

    private var isBlocking = false
    private var target: EntityLivingBase? = null
    private var lastTargetPos: Vec3? = null
    private var lastStrafeYaw = 0.0

    val onGameTick = handler<GameTickEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        target = if (!KillAura.handleEvents()) {
            getNearestTarget()
        } else {
            KillAura.target
        }

        if (smartBlocking) {
            val currentTarget = target
            if (currentTarget == null) {
                if (isBlocking) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
                    isBlocking = false
                }
            } else {
                val heldItem = thePlayer.heldItem?.item
                if (heldItem is ItemSword) {
                    val shouldBlock = currentTarget.hurtTime > 3 && thePlayer.getDistanceToEntityBox(currentTarget) <= blockRange
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, shouldBlock)
                    isBlocking = currentTarget.hurtTime > 3
                } else if (isBlocking) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
                    isBlocking = false
                }
            }
        }
    }

    val onMoveInput = handler<MovementInputEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler
        val currentTarget = target ?: return@handler

        // Combo breaker
        if (comboBreaker && thePlayer.getDistanceToEntityBox(currentTarget) >= breakerAttackRange 
            && !thePlayer.onGround && thePlayer.hurtTime != 0 && currentTarget.hurtTime == 0) {
            event.originalInput.moveForward = -1f
        }

        // Keep combo
        if (keepCombo && thePlayer.getDistanceToEntityBox(currentTarget) < keepComboAttackRange 
            && !currentTarget.onGround && currentTarget.hurtTime != 0 && thePlayer.hurtTime == 0) {
            event.originalInput.moveForward = -1f
        }

        // Adaptive strafe
        if (adaptiveStrafe && (isAttemptingToStrafe() || forceStrafe) && 
            thePlayer.getDistanceToEntityBox(currentTarget) < maxStrafeDistance) {
            val strafeYaw = calculateStrafeYaw(currentTarget)
            fixMovement(event.originalInput, strafeYaw)
        }
    }

    override fun onDisable() {
        lastTargetPos = null
        lastStrafeYaw = 0.0
    }

    private fun calculateStrafeYaw(target: EntityLivingBase): Float {
        val thePlayer = mc.thePlayer
        if (!adaptiveStrafe || thePlayer.hurtTime != 0)
            return thePlayer.rotationYaw

        val targetMotion = lastTargetPos?.let { Vec3(target.posX, target.posY, target.posZ).subtract(it) } ?: Vec3(0.0, 0.0, 0.0)
        lastTargetPos = Vec3(target.posX, target.posY, target.posZ)

        val predictedTarget = Vec3(
            target.posX + targetMotion.xCoord * predictionTicks,
            target.posY + targetMotion.yCoord * predictionTicks,
            target.posZ + targetMotion.zCoord * predictionTicks
        )
        val playerPos = Vec3(thePlayer.posX, thePlayer.posY, thePlayer.posZ)
        val rel = playerPos.subtract(predictedTarget)
        val distanceToPredicted = rel.lengthVector()

        val idealDistance = max(strafeDistance, 2.5f)
        val angleToPredicted = atan2(rel.zCoord, rel.xCoord)
        val angleOffset = if (distanceToPredicted > idealDistance) -45.0 else 45.0
        val targetAngle = angleToPredicted + Math.toRadians(angleOffset)

        val strafeX = predictedTarget.xCoord + cos(targetAngle) * idealDistance
        val strafeZ = predictedTarget.zCoord + sin(targetAngle) * idealDistance

        val targetYaw = Math.toDegrees(atan2(strafeZ - thePlayer.posZ, strafeX - thePlayer.posX)).toFloat()
        val deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - lastStrafeYaw.toFloat())

        lastStrafeYaw = (lastStrafeYaw + MathHelper.clamp_float(deltaYaw, -20f, 20f).toDouble())
        return lastStrafeYaw.toFloat() - 90f
    }

    private fun isAttemptingToStrafe(): Boolean {
        val thePlayer = mc.thePlayer ?: return false
        val currentTarget = target ?: return false

        return thePlayer.movementInput.moveStrafe != 0f || 
               abs(MathHelper.wrapAngleTo180_float(thePlayer.rotationYaw - 
                   getRotationToEntity(currentTarget)[0])) > 25
    }

    private fun getNearestTarget(): EntityLivingBase? {
        val thePlayer = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        return world.loadedEntityList
            .asSequence()
            .filterIsInstance<EntityLivingBase>()
            .filter { shouldTarget(it) && thePlayer.getDistanceToEntityBox(it) <= publicSearchRange * 2 }
            .minByOrNull { thePlayer.getDistanceToEntityBox(it) }
    }

    private fun shouldTarget(entity: EntityLivingBase): Boolean {
        if (entity == mc.thePlayer) return false
        
        return when (entity) {
            is EntityPlayer -> true
            is EntityMob -> false
            is EntityAnimal -> false
            else -> false
        } && !entity.isInvisible
    }

    private fun getRotationToEntity(entity: EntityLivingBase): FloatArray {
        val thePlayer = mc.thePlayer
        val rotation = RotationUtils.toRotation(Vec3(entity.posX, entity.posY + entity.eyeHeight, entity.posZ), false, thePlayer)
        return floatArrayOf(rotation.yaw, rotation.pitch)
    }

    private fun fixMovement(input: MovementInput, yaw: Float) {
        val forward = input.moveForward
        val strafe = input.moveStrafe
        val sinYaw = sin(Math.toRadians(yaw.toDouble())).toFloat()
        val cosYaw = cos(Math.toRadians(yaw.toDouble())).toFloat()
        input.moveForward = forward * cosYaw - strafe * sinYaw
        input.moveStrafe = forward * sinYaw + strafe * cosYaw
    }
}
