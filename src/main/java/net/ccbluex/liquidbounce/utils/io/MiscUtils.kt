/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.utils.io

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.awt.Desktop
import java.awt.Font
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter

import java.awt.FileDialog
import java.awt.Frame

object MiscUtils : MinecraftInstance {

    @JvmStatic
    fun copy(content: String) {
        val selection = StringSelection(content)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
    }

    @JvmStatic
    private fun JTextArea.adjustTextAreaSize() {
        val fontMetrics = getFontMetrics(font)

        val lineSequence = text.lineSequence()
        val lines = lineSequence.count()
        val maxLineWidth = lineSequence.maxOfOrNull { fontMetrics.stringWidth(it) } ?: 0
        val columns = maxLineWidth / fontMetrics.charWidth('m')

        this.rows = lines + 1
        this.columns = columns + 1
    }

    @JvmStatic
    fun generateCrashInfo(): String {
        var base = """
            --- Game crash info ---
            Client: ${LiquidBounce.CLIENT_NAME} ${LiquidBounce.clientVersionText} (${LiquidBounce.clientCommit})
            Time: ${LocalDateTime.now()}
            OS: ${System.getProperty("os.name")} (Version: ${System.getProperty("os.version")}, Arch: ${System.getProperty("os.arch")})
            Java: ${System.getProperty("java.version")} (Vendor: ${System.getProperty("java.vendor")})
        """.trimIndent()

        if (mc.currentServerData != null) {
            val serverData = mc.currentServerData
            base += """
                Server address: ${serverData.serverIP}
                Server version: ${serverData.gameVersion}
            """.trimIndent()
        }

        return base + '\n'
    }

    @JvmStatic
    fun showMessageDialog(title: String, message: Any, messageType: Int = JOptionPane.ERROR_MESSAGE) {
        if (SwingUtilities.isEventDispatchThread()) {
            JOptionPane.showMessageDialog(null, message, title, messageType)
        } else {
            SwingUtilities.invokeLater {
                JOptionPane.showMessageDialog(null, message, title, messageType)
            }
        }
    }

    @JvmStatic
    fun Throwable.showErrorPopup(
        titlePrefix: String = "Exception occurred: ",
        extraContent: String = LocalDateTime.now().toString() + '\n'
    ) {
        if (mc.isFullScreen) mc.toggleFullscreen()

        val exceptionType = javaClass.simpleName

        val title = titlePrefix + exceptionType

        val content = extraContent + "--- Stacktrace ---\n" + stackTraceToString()

        val textArea = JTextArea(content).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = Font("Consolas", Font.PLAIN, 12)
            adjustTextAreaSize()
        }

        val scrollPane = JScrollPane(textArea).apply {
            preferredSize = java.awt.Dimension(800, 600)
        }

        val copyButton = JButton("Copy Text").apply {
            addActionListener {
                copy(content)
                JOptionPane.showMessageDialog(null, "Text copied to clipboard!", "Info", JOptionPane.INFORMATION_MESSAGE)
            }
        }

        val openIssueButton = JButton("Open GitHub Issue").apply {
            addActionListener {
                showURL("${LiquidBounce.CLIENT_GITHUB}/issues/new?template=bug_report.yml&title=%5BBUG%5D+Game+crashed+$exceptionType")
            }
        }

        val buttonPanel = JPanel().apply {
            add(copyButton)
            add(openIssueButton)
        }

        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(scrollPane)
            add(buttonPanel)
        }

        showMessageDialog(title, mainPanel)
    }

    @JvmStatic
    fun showURL(url: String) =
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: IOException) {
            e.showErrorPopup()
        } catch (e: URISyntaxException) {
            e.showErrorPopup()
        }

    private fun combineToFileDialogFilter(fileFilters: Array<out FileFilter>): String? {
        val extensions = mutableSetOf<String>()
        for (filter in fileFilters) {
            if (filter is FileNameExtensionFilter) {
                extensions.addAll(filter.extensions)
            }
        }
        return if (extensions.isEmpty()) null else extensions.joinToString(";") { "*.$it" }
    }

    @JvmStatic // tlz fix, no AI cam on
    private fun fileChooserAction(
        fileFilters: Array<out FileFilter>,
        acceptAll: Boolean,
        mode: Int,
        title: String
    ): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()

        var resultFile: File? = null

        try {
            SwingUtilities.invokeAndWait {
                val parentFrame = Frame()
                parentFrame.isUndecorated = true
                parentFrame.isVisible = true
                parentFrame.toFront()
                parentFrame.isVisible = false

                val dialog = FileDialog(parentFrame, title, mode)
                dialog.directory = FileManager.dir.absolutePath

                dialog.file = combineToFileDialogFilter(fileFilters)

                try {
                    dialog.isVisible = true

                    val fileName = dialog.file
                    val fileDir = dialog.directory

                    if (fileName != null && fileDir != null) {
                        resultFile = File(fileDir, fileName)
                    }
                } finally {
                    parentFrame.dispose()
                }
            }
        } catch (e: Exception) {
            e.showErrorPopup("File Chooser Error (AWT FileDialog):")
            resultFile = null
        }
        return resultFile
    }

    @JvmStatic
    fun openFileChooser(
        vararg fileFilters: FileFilter,
        acceptAll: Boolean = true,
        title: String = "Open File"
    ): File? = fileChooserAction(fileFilters, acceptAll, FileDialog.LOAD, title)

    @JvmStatic
    fun saveFileChooser(
        vararg fileFilters: FileFilter,
        acceptAll: Boolean = true,
        title: String = "Save File"
    ): File? = fileChooserAction(fileFilters, acceptAll, FileDialog.SAVE, title)
}

object FileFilters {
    @JvmField
    val JAVASCRIPT = FileNameExtensionFilter("JavaScript Files", "js")

    @JvmField
    val TEXT = FileNameExtensionFilter("Text Files", "txt") 

    @JvmField
    val IMAGE = FileNameExtensionFilter("Image Files (png)", "png")

    @JvmField
    val ALL_IMAGES = ImageIO.getReaderFormatNames().mapTo(sortedSetOf(), String::lowercase).let {
        FileNameExtensionFilter("Image Files (${it.joinToString()})", *it.toTypedArray())
    }

    @JvmField
    val SHADER = FileNameExtensionFilter("Shader Files (frag, glsl, shader)", "frag", "glsl", "shader")

    @JvmField
    val ARCHIVE = FileNameExtensionFilter("Archive Files (zip)", "zip")
    
    @JvmField
    val FONT = FileNameExtensionFilter("Font Files (ttf, otf)", "ttf", "otf")

}