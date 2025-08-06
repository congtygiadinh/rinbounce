package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.file.FileManager
import org.json.JSONObject
import java.io.File

object MapleAICommand : Command("mapleai") {

    private val configFile = File(FileManager.dir, "mapleai.json")

    init {
        if (!configFile.exists()) {
            configFile.writeText(JSONObject().toString())
        }
    }

    override fun execute(args: Array<String>) {
        if (args.size < 4 || args[1].lowercase() != "set") {
            chatSyntax("mapleai set <key|model> <value>")
            return
        }

        val option = args[2].lowercase()
        val value = args[3]

        try {
            val config = JSONObject(configFile.readText())
            when (option) {
                "key" -> {
                    config.put("apiKey", value)
                    chat("§aMapleAI API key has been set successfully.")
                }
                "model" -> {
                    config.put("model", value)
                    chat("§aMapleAI model has been set to '$value'.")
                }
                else -> {
                    chatSyntax("mapleai set <key|model> <value>")
                    return
                }
            }
            configFile.writeText(config.toString(4))
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("Failed to save MapleAI configuration.", e)
            chat("§cError: Failed to save configuration.")
        }
    }

    fun getApiKey(): String? {
        return try {
            val config = JSONObject(configFile.readText())
            config.optString("apiKey", null)
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("Failed to read MapleAI API key.", e)
            null
        }
    }

    fun getModel(): String? {
        return try {
            val config = JSONObject(configFile.readText())
            config.optString("model", null)
        } catch (e: Exception) {
            ClientUtils.LOGGER.error("Failed to read MapleAI model.", e)
            null
        }
    }
}
