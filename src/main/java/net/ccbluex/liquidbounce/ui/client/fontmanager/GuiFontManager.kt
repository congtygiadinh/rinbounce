package net.ccbluex.liquidbounce.ui.client.fontmanager

import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.lang.translationButton
import net.ccbluex.liquidbounce.lang.translationMenu
import net.ccbluex.liquidbounce.lang.translationText
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.CustomFontInfo
import net.ccbluex.liquidbounce.ui.font.FontInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.drawCenteredString
import net.ccbluex.liquidbounce.utils.io.FileFilters
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.File
import kotlin.math.sin

private const val BACK_BTN_ID = 0
private const val ADD_BTN_ID = 10
private const val REMOVE_BTN_ID = 11
private const val EDIT_BTN_ID = 12

/**
 * @author MukjepScarlet
 */
class GuiFontManager(private val prevGui: GuiScreen) : AbstractScreen() {

    private enum class Status(val text: String) {
        IDLE("§7Idle..."),
        FAILED_TO_LOAD("§cFailed to load font file!"),
        FAILED_TO_REMOVE("§cFailed to remove font info!")
    }

    private var status = Status.IDLE
    private var animationTime = 0f

    private lateinit var fontListView: GuiList
    private lateinit var addButton: GuiButton
    private lateinit var removeButton: GuiButton
    private lateinit var textField: GuiTextField
    private lateinit var nameField: GuiTextField
    private lateinit var sizeField: GuiTextField

    override fun initGui() {
        buttonList.clear()
        textFields.clear()

        val startPositionY = 22
        val leftStartX = 5
        val rightStartX = width - 80

        val textFieldWidth = (width / 8).coerceAtLeast(70)
        textField = textField(2, mc.fontRendererObj, width - textFieldWidth - 10, 10, textFieldWidth, 20) {
            maxStringLength = Int.MAX_VALUE
        }
        nameField = textField(3, mc.fontRendererObj, leftStartX, startPositionY + 24 * 1, textFieldWidth, 20) {
            maxStringLength = Int.MAX_VALUE
        }
        sizeField = textField(4, mc.fontRendererObj, leftStartX, startPositionY + 24 * 2, textFieldWidth, 20) {
            setValidator {
                it.isNullOrBlank() || it.toIntOrNull()?.takeIf { i -> i in 1..500 } != null
            }
            maxStringLength = 3
        }
        +GuiButton(EDIT_BTN_ID, leftStartX, startPositionY + 24 * 3, 70, 20, translationButton("fontManager.edit"))

        addButton = +GuiButton(ADD_BTN_ID, rightStartX, startPositionY + 24 * 1, 70, 20, translationButton("add"))
        removeButton = +GuiButton(REMOVE_BTN_ID, rightStartX, startPositionY + 24 * 2, 70, 20, translationButton("remove"))

        +GuiButton(BACK_BTN_ID, rightStartX, height - 65, 70, 20, translationButton("back"))

        fontListView = GuiList(this).apply {
            registerScrollButtons(7, 8)
        }
    }

    private fun drawGradientBackground() {
        animationTime += 0.02f
        
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        
        val time = animationTime
        val topR = (0.1f + 0.3f * sin(time * 0.5f)).coerceIn(0f, 1f)
        val topG = (0.3f + 0.4f * sin(time * 0.7f + 1f)).coerceIn(0f, 1f)
        val topB = (0.7f + 0.3f * sin(time * 0.3f + 2f)).coerceIn(0f, 1f)
        
        worldRenderer.pos(width.toDouble(), 0.0, zLevel.toDouble()).color(topR, topG, topB, 1.0f).endVertex()
        worldRenderer.pos(0.0, 0.0, zLevel.toDouble()).color(topR, topG, topB, 1.0f).endVertex()
        worldRenderer.pos(0.0, height.toDouble(), zLevel.toDouble()).color(0.9f, 0.95f, 1.0f, 1.0f).endVertex()
        worldRenderer.pos(width.toDouble(), height.toDouble(), zLevel.toDouble()).color(0.9f, 0.95f, 1.0f, 1.0f).endVertex()
        
        tessellator.draw()
        
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        fontListView.handleMouseInput()
    }

    public override fun keyTyped(typedChar: Char, keyCode: Int) {
        this.textFields.forEach {
            if (it.isFocused) {
                it.textboxKeyTyped(typedChar, keyCode)
            }
        }

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> mc.displayGuiScreen(prevGui)
            Keyboard.KEY_UP -> fontListView.selectedSlot -= 1
            Keyboard.KEY_DOWN -> fontListView.selectedSlot += 1
            Keyboard.KEY_TAB -> fontListView.selectedSlot += if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) -1 else 1
            Keyboard.KEY_RETURN -> fontListView.elementClicked(fontListView.selectedSlot, true, 0, 0)
            Keyboard.KEY_NEXT -> fontListView.scrollBy(height - 100)
            Keyboard.KEY_PRIOR -> fontListView.scrollBy(-height + 100)
            Keyboard.KEY_ADD -> actionPerformed(addButton)
            Keyboard.KEY_DELETE, Keyboard.KEY_MINUS -> actionPerformed(removeButton)
            else -> super.keyTyped(typedChar, keyCode)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile {
            drawGradientBackground()
            fontListView.drawScreen(mouseX, mouseY, partialTicks)
            
            // Animated title
            val titleTime = animationTime * 1.5f
            val titleR = (0.2f + 0.4f * sin(titleTime)).coerceIn(0f, 1f)
            val titleG = (0.4f + 0.4f * sin(titleTime + 1f)).coerceIn(0f, 1f)
            val titleB = (0.8f + 0.2f * sin(titleTime + 2f)).coerceIn(0f, 1f)
            val titleColor = ((titleR * 255).toInt() shl 16) or ((titleG * 255).toInt() shl 8) or (titleB * 255).toInt()
            
            Fonts.fontSemibold40.drawCenteredString(translationMenu("fontManager"), width / 2f, 6f, titleColor)
            
            val count = Fonts.customFonts.size
            val text = if (count == 1) {
                translationText("fontManager.customFonts", count)
            } else {
                translationText("fontManager.customFonts.plural", count)
            }
            
            // Glowing text
            val textGlow = (0.6f + 0.4f * sin(animationTime * 2.5f)).coerceIn(0f, 1f)
            val textColor = (0xFFFFFF and 0x00FFFFFF) or ((255 * textGlow).toInt() shl 24)
            
            Fonts.fontSemibold35.drawCenteredString(text, width / 2f, 18f, textColor)
            Fonts.fontSemibold35.drawCenteredString(status.text, width / 2f, 32f, textColor)

            this.textFields.forEach { it.drawTextBox() }
            
            if (nameField.text.isEmpty() && !nameField.isFocused) {
                val placeholderGlow = (0.5f + 0.3f * sin(animationTime * 1.5f)).coerceIn(0f, 1f)
                val placeholderColor = (Color.GRAY.rgb and 0x00FFFFFF) or ((255 * placeholderGlow).toInt() shl 24)
                Fonts.fontSemibold40.drawStringWithShadow(
                    translationText("fontManager.name") + "...", nameField.xPosition + 4f, nameField.yPosition + 7f, placeholderColor
                )
            }
            
            if (sizeField.text.isEmpty() && !sizeField.isFocused) {
                val placeholderGlow = (0.5f + 0.3f * sin(animationTime * 1.5f + 1f)).coerceIn(0f, 1f)
                val placeholderColor = (Color.GRAY.rgb and 0x00FFFFFF) or ((255 * placeholderGlow).toInt() shl 24)
                Fonts.fontSemibold40.drawStringWithShadow(
                    translationText("fontManager.size") + "...", sizeField.xPosition + 4f, sizeField.yPosition + 7f, placeholderColor
                )
            }
            
            if (textField.text.isEmpty() && !textField.isFocused) {
                val placeholderGlow = (0.5f + 0.3f * sin(animationTime * 1.5f + 2f)).coerceIn(0f, 1f)
                val placeholderColor = (Color.GRAY.rgb and 0x00FFFFFF) or ((255 * placeholderGlow).toInt() shl 24)
                Fonts.fontSemibold40.drawStringWithShadow(
                    translationText("fontManager.preview") + "...", textField.xPosition + 4f, 17f, placeholderColor
                )
            } else {
                val font = fontListView.selectedEntry.value
                font.drawCenteredString(
                    textField.text,
                    x = width * 0.5f,
                    y = height - 40f + font.FONT_HEIGHT * 0.5f,
                    color = Color.WHITE.rgb,
                )
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun CustomFontInfo.save() = Fonts.registerCustomAWTFont(this, save = true) ?: run {
        status = Status.FAILED_TO_LOAD
    }

    private fun editFontInfo(fontInfo: FontInfo) {
        val newName = nameField.text.takeIf { it.isNotBlank() }
        val newSize = sizeField.text.toIntOrNull()?.coerceIn(1, 500)

        if (newName == null && newSize == null) {
            return
        }

        val origin = Fonts.removeCustomFont(fontInfo) ?: run {
            status = Status.FAILED_TO_REMOVE
            return
        }

        var edited = origin
        if (newName != null) {
            edited = edited.copy(name = newName)
        }
        if (newSize != null) {
            edited = edited.copy(fontSize = newSize)
        }

        edited.save()
    }

    public override fun actionPerformed(button: GuiButton) {
        if (!button.enabled) return

        when (button.id) {
            BACK_BTN_ID -> mc.displayGuiScreen(prevGui)
            ADD_BTN_ID -> {
                val file = MiscUtils.openFileChooser(FileFilters.FONT, acceptAll = false)?.takeIf { it.isFile } ?: run {
                    status = Status.FAILED_TO_LOAD
                    return
                }

                val directory = FileManager.fontsDir
                val targetFile = File(directory, file.name)
                if (!targetFile.exists()) {
                    file.copyTo(targetFile, overwrite = true)
                }

                val fontFile = targetFile.relativeTo(directory).path
                val defaultInfo = CustomFontInfo(name = file.name, fontFile = fontFile, fontSize = 20)
                defaultInfo.save()
            }
            REMOVE_BTN_ID -> {
                val fontInfo = fontListView.selectedEntry.key.takeIf { it.isCustom } ?: return
                Fonts.removeCustomFont(fontInfo)
            }
            EDIT_BTN_ID -> {
                val fontInfo = fontListView.selectedEntry.key.takeIf { it.isCustom } ?: return
                editFontInfo(fontInfo)
            }
        }
    }

    private inner class GuiList(prevGui: GuiScreen) :
        GuiSlot(mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30) {

        override fun getSize(): Int = Fonts.customFonts.size

        var selectedSlot = -1
            set(value) {
                field = if (size == 0) {
                    -1
                } else {
                    (value + size) % size
                }
            }

        private val defaultEntry = object : Map.Entry<FontInfo, FontRenderer> {
            override val key: FontInfo
                get() = Fonts.minecraftFontInfo

            override val value: FontRenderer
                get() = mc.fontRendererObj
        }

        val selectedEntry: Map.Entry<FontInfo, FontRenderer>
            get() = Fonts.customFonts.entries.elementAtOrElse(selectedSlot) { defaultEntry }

        public override fun elementClicked(clickedElement: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = clickedElement
        }

        override fun isSelected(p0: Int): Boolean = p0 == selectedSlot

        override fun drawBackground() {}

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val (fontInfo, _) = Fonts.customFonts.entries.elementAt(id)

            // Animated font entry colors
            val entryGlow = (0.7f + 0.3f * sin(animationTime * 2f + id * 0.3f)).coerceIn(0f, 1f)
            val entryColor = (Color.WHITE.rgb and 0x00FFFFFF) or ((255 * entryGlow).toInt() shl 24)

            Fonts.minecraftFont.drawCenteredString("${fontInfo.name} - ${fontInfo.size}", width / 2f, y + 2f, entryColor, true)
        }
    }
}
