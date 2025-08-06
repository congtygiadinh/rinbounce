/*
 * RinBounce Hacked Client
 * A modern free open-source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.target

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawHead
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.withClipping
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*

class RinBounce(
    private val roundedRectRadius: Float = 8f, 
    private val borderStrength: Float = 2f,
    private val backgroundColor: Color = Color(20, 20, 20),
    private val healthBarColor1: Color = Color(144, 238, 144),
    private val healthBarColor2: Color = Color(173, 216, 230),
    private val roundHealthBarShape: Boolean = true,
    private val borderColor: Color = Color(35, 35, 35),
    private val textColor: Color = Color(255, 255, 255),
    private val titleFont: Any = Fonts.fontSemibold35,
    private val healthFont: Any = Fonts.fontRegular30,
    private val textShadow: Boolean = false
) : TargetStyle("RinBounce") {
    
    companion object {
        private val GREEN_ENABLED = Color(144, 238, 144)
        private val BLACK_BACKGROUND = Color(20, 20, 20)
        private val BLACK_MODULE_BACKGROUND = Color(35, 35, 35)
        private val WHITE_TEXT = Color(255, 255, 255)
        private val DISABLED_TEXT = Color(180, 180, 180) 
        private val ACTIVE_MODULE_COLOR = Color(173, 216, 230)
    }

    private var animatedGradientStartColor = GREEN_ENABLED
    private var animatedGradientEndColor = WHITE_TEXT

    override fun drawTarget(entity: EntityLivingBase, easingHealth: Float, maxHealth: Float, easingHurtTime: Float) {
        val width = (40f + (entity.name?.let { Fonts.fontSemibold40.getStringWidth(it) } ?: 0)).coerceAtLeast(118F)
        val height = 40f

        // Update animated colors
        val time = System.currentTimeMillis() / 1000.0
        val hueShift = (sin(time * 0.5) * 0.1 + 0.1).toFloat()

        val hsbStart = Color.RGBtoHSB(GREEN_ENABLED.red, GREEN_ENABLED.green, GREEN_ENABLED.blue, null)
        val hsbEnd = Color.RGBtoHSB(WHITE_TEXT.red, WHITE_TEXT.green, WHITE_TEXT.blue, null)

        animatedGradientStartColor = Color.getHSBColor(hsbStart[0] + hueShift, hsbStart[1], hsbStart[2])
        animatedGradientEndColor = Color.getHSBColor(hsbEnd[0] + hueShift, hsbEnd[1], hsbEnd[2])

        // Draw background with modern RinStyle border
        drawRoundedRect(
            0F, 0F, width, height,
            BLACK_BACKGROUND.rgb,
            roundedRectRadius,
            RenderUtils.RoundedCorners.ALL
        )

        // Draw animated health bar
        val healthBarTop = 26F
        val healthBarHeight = 10F
        val healthBarStart = 38F
        val healthBarTotal = (width - 41F).coerceAtLeast(0F)
        val currentWidth = (easingHealth / maxHealth).coerceIn(0F, 1F) * healthBarTotal

        // Draw background bar with RinStyle design
        drawRoundedRect(
            healthBarStart,
            healthBarTop,
            healthBarStart + healthBarTotal,
            healthBarTop + healthBarHeight,
            BLACK_MODULE_BACKGROUND.rgb,
            roundedRectRadius * 0.75f, 
            RenderUtils.RoundedCorners.ALL
        )

        // Draw animated gradient health bar
        if (currentWidth > 0) {
            drawGradientRect(
                healthBarStart.toInt(),
                healthBarTop.toInt(),
                healthBarStart.toInt() + currentWidth.toInt(),
                healthBarTop.toInt() + healthBarHeight.toInt(),
                animatedGradientStartColor.rgb,
                animatedGradientEndColor.rgb,
                0f
            )
        }

        // Draw health percentage with RinStyle font
        val healthPercentage = (easingHealth / maxHealth * 100).toInt()
        val percentageText = "$healthPercentage%"
        val textWidth = (healthFont as net.ccbluex.liquidbounce.ui.font.GameFontRenderer).getStringWidth(percentageText)
        val calcX = healthBarStart + currentWidth - textWidth
        val textX = max(healthBarStart, calcX)
        val textY = healthBarTop - healthFont.fontHeight / 2 - 2F
        healthFont.drawString(percentageText, textX, textY, WHITE_TEXT.rgb)

        // Draw player head with RinStyle animation
        glPushMatrix()
        val scale = 1 - easingHurtTime / 10f
        val f1 = (0.7F..1F).lerpWith(scale)
        val color = if (easingHurtTime > 0) {
            ColorUtils.interpolateColor(Color.RED, WHITE_TEXT, scale)
        } else {
            WHITE_TEXT
        }
        val centerX1 = (4..32).lerpWith(0.5F)
        val midY = (4f..28f).lerpWith(0.5F)

        glTranslatef(centerX1, midY, 0f)
        glScalef(f1, f1, f1)
        glTranslatef(-centerX1, -midY, 0f)

        val entityTexture = mc.renderManager.getEntityRenderObject<net.minecraft.entity.Entity>(entity)?.getEntityTexture(entity)
        if (entityTexture != null) {
            // Draw player head background first
            drawRoundedRect(
                4f, 4f, 32f, 32f,
                BLACK_MODULE_BACKGROUND.rgb,
                roundedRectRadius * 0.6f, 
                RenderUtils.RoundedCorners.ALL
            )
            
            // Then draw player head
            withClipping(main = {
                drawRoundedRect(4f, 4f, 32f, 32f, 0, 4F)
            }, toClip = {
                drawHead(entityTexture, 4, 4, 8f, 8f, 8, 8, 28, 28, 64F, 64F, color)
            })
        }
        glPopMatrix()

        // Draw player name with RinStyle font
        entity.name?.let {
            val displayName = if (entity.health > 0) it else "§c$it"
            (titleFont as net.ccbluex.liquidbounce.ui.font.GameFontRenderer).drawString(
                displayName,
                healthBarStart,
                6F,
                WHITE_TEXT.rgb,
                false
            )
        }
    }

    override fun getBorder(entity: EntityLivingBase, easingHealth: Float, maxHealth: Float): Border {
        val width = (40f + (entity.name?.let { Fonts.fontSemibold40.getStringWidth(it) } ?: 0)).coerceAtLeast(118F)
        return Border(0F, 0F, width, 40F)
    }

    private fun ClosedFloatingPointRange<Float>.lerpWith(alpha: Float): Float {
        return start + (endInclusive - start) * alpha
    }

    private fun IntRange.lerpWith(alpha: Float): Float {
        return first + (last - first) * alpha
    }
}
