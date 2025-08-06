/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.potion.Potion

object Criticals : Module("Criticals", Category.COMBAT) {

    val mode by choices(
        "Mode",
        arrayOf(
            "Packet",
            "NoGround",
            "Jump",
            "LowJump",
            "Grim",
            "BlocksMC",
            "Visual",
            "Blink"
        ),
        "Packet"
    )

    val delay by int("Delay", 0, 0..500)
    private val hurtTime by int("HurtTime", 10, 0..10)
    private val customMotionY by float("Custom-Y", 0.2f, 0.01f..0.42f) { mode == "CustomMotion" }
    
    private val blinkDelay by intRange("BlinkDelay", 300..600, 0..1000) { mode == "Blink" }
    private val blinkRange by float("BlinkRange", 4.0f, 0.0f..10.0f) { mode == "Blink" }
    private var nextBlinkDelay = 0
    private var isBlinkActive = false
    private var enemyInBlinkRange = false
    private val blinkPackets = mutableListOf<C03PacketPlayer>()

    val msTimer = MSTimer()

    override fun onEnable() {
        when (mode) {
            "NoGround" -> mc.thePlayer.tryJump()
            "Blink" -> {
                isBlinkActive = false
                blinkPackets.clear()
                nextBlinkDelay = blinkDelay.random()
                enemyInBlinkRange = false
            }
        }
    }

    override fun onDisable() {
        if (mode == "Blink") {
            isBlinkActive = false
            blinkPackets.forEach { sendPackets(it) }
            blinkPackets.clear()
        }
    }

    val onAttack = handler<AttackEvent> { event ->
        if (event.targetEntity is EntityLivingBase) {
            val thePlayer = mc.thePlayer ?: return@handler
            val entity = event.targetEntity

            if (!thePlayer.onGround || thePlayer.isOnLadder || thePlayer.isInWeb || thePlayer.isInLiquid ||
                thePlayer.ridingEntity != null || entity.hurtTime > hurtTime ||
                Fly.handleEvents() || !msTimer.hasTimePassed(delay)
            )
                return@handler

            val (x, y, z) = thePlayer

            when (mode.lowercase()) {
                "packet" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.0625, z, true),
                        C04PacketPlayerPosition(x, y, z, false)
                    )
                    thePlayer.onCriticalHit(entity)
                }

                "grim" -> {
                    if (!thePlayer.onGround) {
                        sendPackets(
                            C04PacketPlayerPosition(x, y - 0.000001, z, false)
                        )
                        thePlayer.onCriticalHit(entity)
                    }
                }

                "blocksmc" -> {
                    if (thePlayer.ticksExisted % 4 == 0) {
                        sendPackets(
                            C04PacketPlayerPosition(x, y + 0.0011, z, true),
                            C04PacketPlayerPosition(x, y, z, false)
                        )
                        thePlayer.onCriticalHit(entity)
                    }
                }

                "jump" -> {
                    // Base jump motion (0.42) copied from Minecraft vanilla
                    val baseJumpMotion: Double = 0.42
                    if (thePlayer.isPotionActive(Potion.jump)) {
                        val jumpBoostLevel: Int = thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1;
                        thePlayer.motionY = baseJumpMotion + (jumpBoostLevel * 0.1);
                    } else {
                        thePlayer.motionY = baseJumpMotion;
                    }
                    thePlayer.onCriticalHit(entity);
                }

                "lowjump" -> {
                    // Smaller jump height to stay close to ground
                    thePlayer.motionY = 0.1
                    thePlayer.fallDistance = 0.1f
                    thePlayer.onGround = false
                    thePlayer.onCriticalHit(entity)
                }

                "visual" -> thePlayer.onCriticalHit(entity)
            }

            msTimer.reset()
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        val thePlayer = mc.thePlayer ?: return@handler

        when {
            mode == "NoGround" && packet is C03PacketPlayer -> {
                packet.onGround = false
            }
            mode == "Blink" -> {
                if (!enemyInBlinkRange || thePlayer.onGround || thePlayer.isInLiquid || thePlayer.isInWeb) {
                    isBlinkActive = false
                    return@handler
                }

                if (packet is C03PacketPlayer) {
                    if (msTimer.hasTimePassed(nextBlinkDelay.toLong())) {
                        nextBlinkDelay = blinkDelay.random()
                        blinkPackets.clear()
                        isBlinkActive = false
                        return@handler
                    }

                    when (packet) {
                        is C03PacketPlayer.C04PacketPlayerPosition,
                        is C03PacketPlayer.C06PacketPlayerPosLook,
                        is C03PacketPlayer.C05PacketPlayerLook -> {
                            event.cancelEvent()
                            blinkPackets.add(packet)
                            isBlinkActive = true
                        }
                    }
                }
            }
        }
    }

    override val tag
        get() = mode
}
