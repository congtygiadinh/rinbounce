/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.attack.CombatCheck
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.config.*
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.world.WorldSettings

object RinReach : Module("RinReach", Category.COMBAT) {

    private val modeValue by choices("Mode", arrayOf("RinIntave", "RinFakePlayer"), "RinFakePlayer")
    private val aura by boolean("Aura", false)
    private val pulseDelayValue by int("PulseDelay", 200, 50..500) { modeValue == "RinFakePlayer" || modeValue == "RinIntave" }
    private val intaveTestHurtTimeValue by int("Intave-Packets", 5, 0..30) { modeValue == "RinIntave" }

    private var fakePlayer: EntityOtherPlayerMP? = null
    private var currentTarget: EntityLivingBase? = null
    private var shown = false
    private val pulseTimer = MSTimer()

    override fun onEnable() {
    }

    override fun onDisable() {
        removeFakePlayer()
    }

    private fun removeFakePlayer() {
        fakePlayer?.let {
            currentTarget = null
            MinecraftInstance.mc.theWorld?.removeEntity(it)
            fakePlayer = null
            shown = false
        }
    }

    private fun attackEntity(entity: EntityLivingBase) {
        MinecraftInstance.mc.thePlayer?.run {
            swingItem()
            MinecraftInstance.mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
            if (MinecraftInstance.mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                attackTargetEntityWithCurrentItem(entity)
            }
        }
    }

    private fun createFakePlayer(target: EntityLivingBase) {
        val world = MinecraftInstance.mc.theWorld ?: return
        val playerInfo = MinecraftInstance.mc.netHandler.getPlayerInfo(target.uniqueID) ?: return
        val faker = EntityOtherPlayerMP(world, playerInfo.gameProfile).apply {
            rotationYawHead = target.rotationYawHead
            renderYawOffset = target.renderYawOffset
            copyLocationAndAnglesFrom(target)
            health = target.health
            (0..4).forEach { index ->
                target.getEquipmentInSlot(index)?.let { setCurrentItemOrArmor(index, it) }
            }
        }
        world.addEntityToWorld(-1337, faker)
        fakePlayer = faker
        shown = true
    }


val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity as? EntityLivingBase ?: return@handler
        CombatCheck.setTarget(target)

        when (modeValue) {
            "RinIntave", "RinFakePlayer" -> {
                if (fakePlayer == null) {
                    currentTarget = target
                    createFakePlayer(target)
                } else if (event.targetEntity == fakePlayer) {
                    currentTarget?.let { attackEntity(it) }
                    event.cancelEvent()
                } else {
                    removeFakePlayer()
                    currentTarget = target
                    createFakePlayer(target)
                }
            }
        }
    }

    val onUpdate = handler<UpdateEvent> { event ->
        CombatCheck.updateCombatState()
        if (MinecraftInstance.mc.thePlayer == null || currentTarget == null || !CombatCheck.inCombat) {
            removeFakePlayer()
            return@handler
        }

        if (aura && !LiquidBounce.moduleManager.getModule(KillAura::class.java)!!.state) {
            removeFakePlayer()
            return@handler
        }

        when (modeValue) {
            "RinIntave" -> {
                fakePlayer?.let { faker ->
                    currentTarget?.let { target ->
                        if (!faker.isEntityAlive || target.isDead || !target.isEntityAlive) {
                            removeFakePlayer()
                        } else {
                            faker.health = target.health
                            (0..4).forEach { index ->
                                target.getEquipmentInSlot(index)?.let { faker.setCurrentItemOrArmor(index, it) }
                            }
                            if (MinecraftInstance.mc.thePlayer.ticksExisted % intaveTestHurtTimeValue == 0) {
                                faker.rotationYawHead = target.rotationYawHead
                                faker.renderYawOffset = target.renderYawOffset
                                faker.copyLocationAndAnglesFrom(target)
                                pulseTimer.reset()
                            }
                        }
                    }
                }

                if (!shown && currentTarget != null && MinecraftInstance.mc.netHandler.getPlayerInfo(currentTarget?.uniqueID)?.gameProfile != null) {
                    createFakePlayer(currentTarget!!)
                }
            }
            "RinFakePlayer" -> {
                fakePlayer?.let { faker ->
                    currentTarget?.let { target ->
                        if (!faker.isEntityAlive || target.isDead || !target.isEntityAlive) {
                            removeFakePlayer()
                        } else {
                            faker.health = target.health
                            (0..4).forEach { index ->
                                target.getEquipmentInSlot(index)?.let { faker.setCurrentItemOrArmor(index, it) }
                            }
                            if (pulseTimer.hasTimePassed(pulseDelayValue.toLong())) {
                                faker.rotationYawHead = target.rotationYawHead
                                faker.renderYawOffset = target.renderYawOffset
                                faker.copyLocationAndAnglesFrom(target)
                                pulseTimer.reset()
                            }
                        }
                    }
                }

                if (!shown && currentTarget != null && MinecraftInstance.mc.netHandler.getPlayerInfo(currentTarget?.uniqueID)?.gameProfile != null) {
                    createFakePlayer(currentTarget!!)
                }
            }
        }
    }
}