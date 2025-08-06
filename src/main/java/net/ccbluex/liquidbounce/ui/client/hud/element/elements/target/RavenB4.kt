package net.ccbluex.liquidbounce.ui.client.hud.element.elements.target

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.abs
/**
 * @author Fyxar
 * @reason RavenB4 target hud I lost my skill code
 */
class RavenB4(
    private val roundedRectRadius: Float,
    private val borderStrength: Float,
    private val backgroundColor: Color,
    private val healthBarColor1: Color,
    private val healthBarColor2: Color,
    private val roundHealthBarShape: Boolean,
    private val borderColor: Color,
    private val textColor: Color,
    private val titleFont: Any,
    private val healthFont: Any,
    private val textShadow: Boolean
) : TargetStyle("RavenB4") {

    private val decimalFormat = DecimalFormat("0.0")
    private var easingHealth = 0f

    private fun updateAnim(targetHealth: Float) {
        val healthDiff = targetHealth - easingHealth
        easingHealth += if (abs(healthDiff) < 0.01f) {
            healthDiff
        } else {
            healthDiff * 0.025f
        }
    }

    private fun fadeAlpha(baseAlpha: Int): Int {
        return baseAlpha
    }

    override fun drawTarget(entity: EntityLivingBase, easingHealth: Float, maxHealth: Float, easingHurtTime: Float) {
        try {
            val font = Fonts.minecraftFont
            val healthString = decimalFormat.format(entity.health)
            val healthLength = font.getStringWidth(healthString)
            val nameLength = font.getStringWidth(entity.displayName.formattedText)
            val totalWidth = nameLength + healthLength + 23f
            val totalHeight = 35f

            GlStateManager.pushMatrix()
            updateAnim(entity.health)

            drawRoundedBorderRect(
                0f, 0f, totalWidth, totalHeight,
                2f,
                Color(0, 0, 0, fadeAlpha(100)).rgb,
                Color(255, 255, 255, fadeAlpha(255)).rgb,
                4f
            )

            GlStateManager.enableBlend()

            font.drawStringWithShadow(
                entity.displayName.formattedText,
                6f, 8f,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )

            val isWinning = entity.health <= mc.thePlayer.health
            font.drawStringWithShadow(
                if (isWinning) "W" else "L",
                nameLength + healthLength + 11.6f, 8f,
                if (isWinning)
                    Color(0, 255, 0, fadeAlpha(255)).rgb
                else
                    Color(255, 0, 0, fadeAlpha(255)).rgb
            )

            font.drawStringWithShadow(
                healthString,
                nameLength + 8f, 8f,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )

            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()

            drawRoundedRect(
                5f, 25f, nameLength + healthLength + 18f, 29.5f,
                Color(0, 0, 0, fadeAlpha(110)).rgb,
                2f
            )

            val easingWidth = 5f + (this.easingHealth / entity.maxHealth) * (nameLength + healthLength + 13f)
            if (easingWidth > 5f) {
                drawRoundedRect(
                    5f, 25f, easingWidth, 29.5f,
                    Color(255, 255, 255, fadeAlpha(100)).rgb,
                    2f
                )
            }

            val currentWidth = 5f + (entity.health / entity.maxHealth) * (nameLength + healthLength + 13f)
            if (currentWidth > 5f) {
                drawRoundedRect(
                    5f, 25f, currentWidth, 29.5f,
                    Color(255, 255, 255, fadeAlpha(255)).rgb,
                    2f
                )
            }

            GlStateManager.popMatrix()

        } catch (e: Exception) {
            drawFallback(entity, easingHealth, maxHealth, easingHurtTime)
        }
    }

    private fun drawFallback(entity: EntityLivingBase, easingHealth: Float, maxHealth: Float, easingHurtTime: Float) {
        try {
            val font = Fonts.minecraftFont
            val healthString = decimalFormat.format(entity.health)
            val nameLength = font.getStringWidth(entity.displayName.formattedText)
            val totalWidth = 60f + nameLength
            val totalHeight = 28f

            GlStateManager.pushMatrix()

            RenderUtils.drawRect(
                0f, 0f, totalWidth, totalHeight,
                Color(0, 0, 0, fadeAlpha(100)).rgb
            )

            GlStateManager.enableBlend()

            val winLoseText = if (entity.health < mc.thePlayer.health) " §AW§F" else " §CL§F"

            font.drawStringWithShadow(
                "Target: ${entity.displayName.formattedText}$winLoseText",
                4f, 3f,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )

            font.drawStringWithShadow(
                "Health: ", 4.3f, 16f,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )

            font.drawStringWithShadow(
                healthString, 42f, 16f,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )

            val healthPercentage = (entity.health / entity.maxHealth).coerceIn(0f, 1f)
            RenderUtils.drawRect(
                0f, 26f, totalWidth * healthPercentage, 28f,
                Color(255, 255, 255, fadeAlpha(255)).rgb
            )

            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()

        } catch (e: Exception) {
            val font = Fonts.minecraftFont
            font.drawStringWithShadow(
                "Target: ${entity.displayName.formattedText}",
                4f, 3f, Color(255, 255, 255, fadeAlpha(255)).rgb
            )
        }
    }

    override fun getBorder(entity: EntityLivingBase, easingHealth: Float, maxHealth: Float): Border? {
        return try {
            val font = Fonts.minecraftFont
            val nameWidth = font.getStringWidth(entity.displayName.formattedText)
            val healthWidth = font.getStringWidth(decimalFormat.format(entity.health))
            val totalWidth = nameWidth + healthWidth + 23f

            Border(0f, 0f, totalWidth, 35f)
        } catch (e: Exception) {
            Border(0f, 0f, 100f, 35f)
        }
    }
}