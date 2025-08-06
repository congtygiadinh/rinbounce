/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.LiquidBounce.clientVersionText
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.I18n
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.*
import kotlin.random.Random

class GuiMainMenu : AbstractScreen() {

    // ? cailonmaskidskidconcak ALL ANIMATION VARIABLES FROM VERSION 26
    private var animationTime = 0f
    private var fadeAlpha = 255
    private var fadeIn = true
    private val particles = mutableListOf<OceanParticle>()
    private val smokeParticles = mutableListOf<SmokeParticle>()
    private val buttonAnimations = mutableMapOf<Int, Float>() // Button hover animations

    // ? cailonmaskidskidconcak OCEAN PARTICLE CLASS
    data class OceanParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        var maxLife: Float,
        var size: Float,
        val color: Color
    )

    // ? cailonmaskidskidconcak SMOKE PARTICLE CLASS  
    data class SmokeParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        var maxLife: Float,
        var size: Float,
        var rotation: Float,
        var rotationSpeed: Float
    )

    companion object {
        var lastWarningTime: Long? = null
        private val warningInterval = TimeUnit.DAYS.toMillis(7)

        fun shouldShowWarning() = lastWarningTime == null || Instant.now().toEpochMilli() - lastWarningTime!! > warningInterval
    }

    override fun initGui() {
        // ? cailonmaskidskidconcak CENTERED BUTTONS
        val buttonWidth = 200
        val buttonHeight = 32
        val centerX = width / 2 - buttonWidth / 2
        val startY = height / 2 - 80

        +GuiButton(1, centerX, startY, buttonWidth, buttonHeight, I18n.format("menu.singleplayer"))
        +GuiButton(2, centerX, startY + 40, buttonWidth, buttonHeight, I18n.format("menu.multiplayer"))
        +GuiButton(100, centerX, startY + 80, buttonWidth, buttonHeight, "Alt Manager")
        +GuiButton(0, centerX, startY + 120, buttonWidth, buttonHeight, I18n.format("menu.options"))
        +GuiButton(4, centerX, startY + 160, buttonWidth, buttonHeight, I18n.format("menu.quit"))

        // Initialize button animations
        buttonAnimations.clear()

        // ? cailonmaskidskidconcak INITIALIZE ALL PARTICLES FROM VERSION 26
        particles.clear()
        smokeParticles.clear()

        // Create initial ocean particles
        repeat(50) {
            particles.add(createOceanParticle())
        }

        // Create initial smoke particles
        repeat(30) {
            smokeParticles.add(createSmokeParticle())
        }
    }

    // ? cailonmaskidskidconcak CUSTOM BUTTON RENDERING
    private fun drawCustomButton(button: GuiButton, mouseX: Int, mouseY: Int) {
        if (!button.visible) return

        val isHovered = mouseX >= button.xPosition && mouseY >= button.yPosition && 
                       mouseX < button.xPosition + button.width && mouseY < button.yPosition + button.height

        // Update hover animation
        val currentAnim = buttonAnimations.getOrDefault(button.id, 0f)
        val targetAnim = if (isHovered) 1f else 0f
        val newAnim = currentAnim + (targetAnim - currentAnim) * 0.15f
        buttonAnimations[button.id] = newAnim

        val x = button.xPosition.toFloat()
        val y = button.yPosition.toFloat()
        val width = button.width.toFloat()
        val height = button.height.toFloat()
        val radius = 8f

        // ? cailonmaskidskidconcak BUTTON BACKGROUND
        val bgAlpha = (140 + 80 * newAnim).toInt()
        val bgColor = Color(20, 20, 30, bgAlpha)

        RenderUtils.drawRoundedRect(
            x, y, x + width, y + height,
            bgColor.rgb,
            radius
        )

        // ? cailonmaskidskidconcak GRADIENT BORDER ON HOVER
        if (newAnim > 0f) {
            val borderWidth = 2f * newAnim

            // Create gradient border effect
            val steps = 20
            for (i in 0 until steps) {
                val progress = i.toFloat() / steps
                val angle = progress * 2 * PI + animationTime * 2

                // Gradient from white to light blue
                val r = (255 * (1f - progress * 0.3f)).toInt()
                val g = (255 * (1f - progress * 0.1f)).toInt()
                val b = 255
                val alpha = (120 * newAnim * (1f - progress * 0.5f)).toInt()

                val borderColor = Color(r, g, b, alpha)

                RenderUtils.drawRoundedRect(
                    x - borderWidth * (1f + progress * 0.5f), 
                    y - borderWidth * (1f + progress * 0.5f),
                    x + width + borderWidth * (1f + progress * 0.5f), 
                    y + height + borderWidth * (1f + progress * 0.5f),
                    borderColor.rgb,
                    radius + borderWidth * (1f + progress * 0.5f)
                )
            }
        }

        // ? cailonmaskidskidconcak INNER GLOW EFFECT
        if (newAnim > 0f) {
            val glowColor = Color(100, 200, 255, (40 * newAnim).toInt())
            RenderUtils.drawRoundedRect(
                x + 2, y + 2, x + width - 2, y + height - 2,
                glowColor.rgb,
                radius - 2
            )
        }

        // ? cailonmaskidskidconcak BUTTON TEXT
        val font = mc.fontRendererObj
        val textColor = if (button.enabled) {
            Color(255, 255, 255, (220 + 35 * newAnim).toInt())
        } else {
            Color(120, 120, 120, 200)
        }

        val textX = x + width / 2 - font.getStringWidth(button.displayString) / 2
        val textY = y + height / 2 - font.FONT_HEIGHT / 2

        // Text shadow
        font.drawString(button.displayString, (textX + 1).toInt(), (textY + 1).toInt(), Color(0, 0, 0, 150).rgb)
        font.drawString(button.displayString, textX.toInt(), textY.toInt(), textColor.rgb)
    }

    // ? cailonmaskidskidconcak LIGHTER OCEAN BACKGROUND
    private fun drawOceanBackground() {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.shadeModel(GL11.GL_SMOOTH)

        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)

        // ? cailonmaskidskidconcak LIGHTER ANIMATED OCEAN COLORS
        val time = animationTime * 0.5f
        val deepOcean = Color(
            (40 + 30 * sin(time)).toInt().coerceIn(0, 255), 
            (80 + 40 * sin(time * 0.7f)).toInt().coerceIn(0, 255), 
            (120 + 50 * cos(time * 0.8f)).toInt().coerceIn(0, 255)
        )
        val lightOcean = Color(
            (80 + 50 * sin(time * 1.2f)).toInt().coerceIn(0, 255), 
            (140 + 60 * cos(time * 0.9f)).toInt().coerceIn(0, 255), 
            (180 + 70 * sin(time * 1.1f)).toInt().coerceIn(0, 255)
        )
        val surface = Color(
            (150 + 70 * sin(time * 0.8f)).toInt().coerceIn(0, 255), 
            (200 + 55 * cos(time)).toInt().coerceIn(0, 255), 
            (240 + 15 * sin(time * 1.3f)).toInt().coerceIn(0, 255)
        )

        // Multi-layer gradient
        worldRenderer.pos(width.toDouble(), 0.0, 0.0).color(surface.red, surface.green, surface.blue, 255).endVertex()
        worldRenderer.pos(0.0, 0.0, 0.0).color(surface.red, surface.green, surface.blue, 255).endVertex()
        worldRenderer.pos(0.0, height * 0.3, 0.0).color(lightOcean.red, lightOcean.green, lightOcean.blue, 255).endVertex()
        worldRenderer.pos(width.toDouble(), height * 0.3, 0.0).color(lightOcean.red, lightOcean.green, lightOcean.blue, 255).endVertex()

        worldRenderer.pos(width.toDouble(), height * 0.3, 0.0).color(lightOcean.red, lightOcean.green, lightOcean.blue, 255).endVertex()
        worldRenderer.pos(0.0, height * 0.3, 0.0).color(lightOcean.red, lightOcean.green, lightOcean.blue, 255).endVertex()
        worldRenderer.pos(0.0, height.toDouble(), 0.0).color(deepOcean.red, deepOcean.green, deepOcean.blue, 255).endVertex()
        worldRenderer.pos(width.toDouble(), height.toDouble(), 0.0).color(deepOcean.red, deepOcean.green, deepOcean.blue, 255).endVertex()

        tessellator.draw()

        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.enableTexture2D()
    }

    // ? cailonmaskidskidconcak OCEAN PARTICLES SYSTEM
    private fun updateAndRenderParticles() {
        particles.removeAll { particle ->
            particle.life -= 0.016f
            particle.x += particle.vx
            particle.y += particle.vy
            particle.vy += 0.01f

            if (particle.life > 0) {
                val alpha = (particle.life / particle.maxLife * 255).toInt().coerceIn(0, 255)
                val particleColor = Color(particle.color.red, particle.color.green, particle.color.blue, alpha)

                drawRect(
                    particle.x - particle.size/2, particle.y - particle.size/2,
                    particle.x + particle.size/2, particle.y + particle.size/2,
                    particleColor.rgb
                )
            }

            particle.life <= 0
        }

        if (particles.size < 50 && Random.nextFloat() < 0.3f) {
            particles.add(createOceanParticle())
        }
    }

    // ? cailonmaskidskidconcak SMOKE PARTICLES SYSTEM
    private fun updateAndRenderSmoke() {
        smokeParticles.removeAll { smoke ->
            smoke.life -= 0.02f
            smoke.x += smoke.vx
            smoke.y += smoke.vy
            smoke.rotation += smoke.rotationSpeed
            smoke.size += 0.1f

            if (smoke.life > 0) {
                val alpha = (smoke.life / smoke.maxLife * 100).toInt().coerceIn(0, 100)
                val smokeColor = Color(150, 200, 255, alpha)

                GlStateManager.pushMatrix()
                GlStateManager.translate(smoke.x, smoke.y, 0f)
                GlStateManager.rotate(smoke.rotation, 0f, 0f, 1f)

                drawRect(
                    -smoke.size/2, -smoke.size/2,
                    smoke.size/2, smoke.size/2,
                    smokeColor.rgb
                )

                GlStateManager.popMatrix()
            }

            smoke.life <= 0
        }

        if (smokeParticles.size < 30 && Random.nextFloat() < 0.2f) {
            smokeParticles.add(createSmokeParticle())
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        animationTime += 0.016f

        // ? cailonmaskidskidconcak LIGHTER OCEAN BACKGROUND
        drawOceanBackground()

        // ? cailonmaskidskidconcak PARTICLES AND EFFECTS
        updateAndRenderParticles()
        updateAndRenderSmoke()

        // ? cailonmaskidskidconcak CUSTOM BUTTON RENDERING
        for (button in buttonList) {
            drawCustomButton(button as GuiButton, mouseX, mouseY)
        }

        // ? cailonmaskidskidconcak VERSION TEXT (TOP LEFT)
        mc.fontRendererObj.drawStringWithShadow(
            "b1.2.0", 10f, 10f, 0xFFFFFF

        )

        // ? cailonmaskidskidconcak RESPONSIVE RINBOUNCE TITLE
        val liquidBounceTitle = "RinBounce"

        // Calculate button start position (same as initGui)
        val buttonStartY = height / 2 - 80

        // Responsive scale based on screen height
        val baseScale = 4.0f
        val minScale = 2.0f
        val maxScale = 4.5f
        val titleScale = when {
            height < 400 -> minScale // Very small screens
            height < 600 -> baseScale * 0.7f // Small screens  
            height < 800 -> baseScale // Normal screens
            else -> maxScale // Large screens
        }.coerceIn(minScale, maxScale)

        // Calculate title height when scaled
        val titleHeight = mc.fontRendererObj.FONT_HEIGHT * titleScale

        // Position title above buttons with proper spacing
        val titleSpacing = 20f // Space between title and first button
        val titleY = buttonStartY - titleHeight - titleSpacing

        // Make sure title doesn't go above screen
        val finalTitleY = maxOf(titleHeight + 10f, titleY)

        GlStateManager.pushMatrix()
        GlStateManager.scale(titleScale, titleScale, titleScale)

        val scaledTitleX = (width / 2f - mc.fontRendererObj.getStringWidth(liquidBounceTitle) * titleScale / 2f) / titleScale
        val scaledTitleY = finalTitleY / titleScale

        // Animated color for title
        val titleColorR = (200 + 55 * sin(animationTime * 0.8f)).toInt().coerceIn(0, 255)
        val titleColorG = (220 + 35 * cos(animationTime * 1.2f)).toInt().coerceIn(0, 255)
        val titleColorB = 255
        val titleColor = Color(titleColorR, titleColorG, titleColorB).rgb

        mc.fontRendererObj.drawStringWithShadow(liquidBounceTitle, scaledTitleX, scaledTitleY, titleColor)
        GlStateManager.popMatrix()

        // ? cailonmaskidskidconcak CREDIT TEXT (BOTTOM RIGHT)
        val creditText = "credit; [CCBlueX, RatterMC]"
        mc.fontRendererObj.drawStringWithShadow(
            creditText,
            width - mc.fontRendererObj.getStringWidth(creditText) - 10f,
            height - mc.fontRendererObj.FONT_HEIGHT - 10f,
            0xAAFFFF
        )

        // ? cailonmaskidskidconcak FADE EFFECT
        if (fadeIn) {
            drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color(0, 50, 100, fadeAlpha).rgb)
            fadeAlpha -= 3
            if (fadeAlpha <= 0) {
                fadeIn = false
            }
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
        }
    }

    // ? cailonmaskidskidconcak HELPER FUNCTIONS
    private fun createOceanParticle(): OceanParticle {
        val colors = listOf(
            Color(100, 200, 255),
            Color(150, 220, 255),
            Color(200, 240, 255),
            Color(0, 180, 255)
        )

        return OceanParticle(
            x = Random.nextFloat() * width,
            y = Random.nextFloat() * height,
            vx = (Random.nextFloat() - 0.5f) * 2f,
            vy = (Random.nextFloat() - 0.5f) * 2f,
            life = Random.nextFloat() * 3f + 2f,
            maxLife = Random.nextFloat() * 3f + 2f,
            size = Random.nextFloat() * 3f + 1f,
            color = colors.random()
        )
    }

    private fun createSmokeParticle(): SmokeParticle {
        return SmokeParticle(
            x = Random.nextFloat() * width,
            y = height + 10f,
            vx = (Random.nextFloat() - 0.5f) * 1f,
            vy = -Random.nextFloat() * 2f - 1f,
            life = Random.nextFloat() * 4f + 3f,
            maxLife = Random.nextFloat() * 4f + 3f,
            size = Random.nextFloat() * 2f + 1f,
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = (Random.nextFloat() - 0.5f) * 5f
        )
    }

    override fun doesGuiPauseGame(): Boolean = false
}