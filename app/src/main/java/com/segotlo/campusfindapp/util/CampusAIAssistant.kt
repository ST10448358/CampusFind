package com.segotlo.campusfindapp.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object CampusAIAssistant {

    private const val GEMINI_API_KEY = "AQ.Ab8RN6Lc9DUXO6HMb9VWR2AWtrunG4el0qf8rocnlD5j6gqIA"
    private const val PROJECT_NUMBER = "205707963611"

    private const val VERTEX_URL = "https://us-central1-aiplatform.googleapis.com/v1/projects/$PROJECT_NUMBER/locations/us-central1/publishers/google/models/gemini-1.5-flash:generateContent"
    private const val AI_STUDIO_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    private const val SYSTEM_PROMPT = "You are CampusBot AI, an intelligent virtual assistant for CampusFind, a university lost-and-found mobile app. You help students and staff report lost items, report found items, understand AI-assisted matching, submit claims with proof of ownership, and collect items at the Campus Main Gate Desk (Extension 4400). Always keep your answers polite, concise, helpful, and focused on campus lost & found workflows."

    suspend fun askAI(userQuery: String): String = withContext(Dispatchers.IO) {
        try {
            val isBearer = GEMINI_API_KEY.startsWith("AQ.")
            val urlString = if (isBearer) VERTEX_URL else "$AI_STUDIO_URL?key=$GEMINI_API_KEY"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                if (isBearer) {
                    setRequestProperty("Authorization", "Bearer $GEMINI_API_KEY")
                }
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        if (isBearer) put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "$SYSTEM_PROMPT\n\nUser Question: $userQuery"))
                        })
                    })
                })
            }

            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }

                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "I'm here to help with your lost & found inquiries!")
                    }
                }
                "I'm here to help with your lost & found inquiries!"
            } else {
                "API Error ($responseCode). Please check your credentials."
            }
        } catch (e: Throwable) {
            "Network error or offline mode active. Please check your connection."
        }
    }
}
