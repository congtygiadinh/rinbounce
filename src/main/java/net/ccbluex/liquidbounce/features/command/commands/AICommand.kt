package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object AICommand : Command("ai") {

    override fun execute(args: Array<String>) {
        if (args.size < 2) {
            chatSyntax("ai <question>")
            return
        }

        val apiKey = MapleAICommand.getApiKey()
        if (apiKey.isNullOrEmpty()) {
            chat("§cError: MapleAI API key is not set. Use .mapleai <key> to set it.")
            return
        }

        val question = args[1]
        val model = "gpt-4o"

        val client = OkHttpClient()
        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", listOf(
                mapOf("role" to "system", "content" to "You are a helpful assistant."),
                mapOf("role" to "user", "content" to question)
            ))
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(LiquidBounce.MAPLEAI_API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                chat("§cError: Failed to connect to MapleAI API.")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    chat("§cError: ${response.code} - ${response.message}")
                    return
                }

                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val reply = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                    chat("§aAI Response: §7$reply")
                } else {
                    chat("§cError: Empty response from API.")
                }
            }
        })
    }
}