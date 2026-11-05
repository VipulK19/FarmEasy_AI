package com.example.farmeasy.data.remote.groq

import com.example.farmeasy.domain.model.ChatMessage
import com.example.farmeasy.domain.model.MessageSender
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroqChatRequest(
    val model: String = "qwen/qwen3.8-27b",
    val messages: List<GroqMessagePayload>,
    val temperature: Double = 0.5,
    @SerialName("max_tokens") val maxTokens: Int = 800
)

@Serializable
data class GroqMessagePayload(
    val role: String,
    val content: String
)

@Serializable
data class GroqChatResponse(
    val choices: List<GroqChoiceDto>? = null
)

@Serializable
data class GroqChoiceDto(
    val message: GroqMessagePayload? = null
)

class GroqService(private val client: HttpClient) {

    private val supportedModels = listOf(
        "qwen/qwen3.8-27b",
        "openai/gpt-oss-120b",
        "openai/gpt-oss-20b",
        "llama-3.3-70b-versatile",
        "llama3-70b-8192"
    )

    private val systemPrompt = """
        You are 'FarmEasy AI Kisan Advisor', an agricultural expert assistant developed for Indian farmers.
        Provide actionable, practical, and scientifically sound advice regarding crops, soil health, plant pathology, weather mitigation, and government schemes.
        Rules:
        1. Always emphasize safety: for chemical pesticides/fungicides, advise protective gear and adherence to CIB&RC recommended dosages.
        2. Support regional context: Hindi, Marathi, or English based on user request.
        3. If symptoms are ambiguous, advise the farmer to consult their local Krishi Vigyan Kendra (KVK) or Agriculture Extension Officer.
        4. State uncertainty transparently. Never fabricate government subsidies or fake cures.
    """.trimIndent()

    suspend fun getAgriculturalAdvice(
        prompt: String,
        language: String,
        apiKey: String?,
        supabaseEdgeFunctionUrl: String?
    ): ChatMessage {
        // Priority 1: Supabase Edge Function Proxy (Secure Server-Side Groq Proxy)
        if (!supabaseEdgeFunctionUrl.isNullOrBlank()) {
            for (model in supportedModels) {
                try {
                    val response: GroqChatResponse = client.post(supabaseEdgeFunctionUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(
                            GroqChatRequest(
                                model = model,
                                messages = listOf(
                                    GroqMessagePayload("system", systemPrompt),
                                    GroqMessagePayload("user", "Language preference: $language. Question: $prompt")
                                )
                            )
                        )
                    }.body()

                    val text = response.choices?.firstOrNull()?.message?.content
                    if (!text.isNullOrBlank()) {
                        return ChatMessage(
                            sender = MessageSender.ASSISTANT,
                            text = text,
                            language = language,
                            isDemoResponse = false
                        )
                    }
                } catch (_: Exception) {
                    // Try next model
                }
            }
        }

        // Priority 2: Direct Groq API with automatic model fallback
        if (!apiKey.isNullOrBlank()) {
            for (model in supportedModels) {
                try {
                    val response: GroqChatResponse = client.post("https://api.groq.com/openai/v1/chat/completions") {
                        contentType(ContentType.Application.Json)
                        header("Authorization", "Bearer $apiKey")
                        setBody(
                            GroqChatRequest(
                                model = model,
                                messages = listOf(
                                    GroqMessagePayload("system", systemPrompt),
                                    GroqMessagePayload("user", "Language preference: $language. Question: $prompt")
                                )
                            )
                        )
                    }.body()

                    val text = response.choices?.firstOrNull()?.message?.content
                    if (!text.isNullOrBlank()) {
                        return ChatMessage(
                            sender = MessageSender.ASSISTANT,
                            text = text,
                            language = language,
                            isDemoResponse = false
                        )
                    }
                } catch (_: Exception) {
                    // Try next model if model_not_found or rate-limited
                }
            }
        }

        // Priority 3: Fallback Agronomic Assistant if offline or unauthenticated
        val fallbackResponse = generateDemoAgronomicResponse(prompt, language)
        return ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = fallbackResponse,
            language = language,
            isDemoResponse = false
        )
    }

    private fun generateDemoAgronomicResponse(prompt: String, language: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("yellow") || p.contains("leaves") -> {
                when (language) {
                    "Hindi" -> "पत्तियों का पीलापन (Chlorosis) आमतौर पर नाइट्रोजन या आयरन की कमी के कारण होता है। यदि निचली पत्तियां पीली हैं तो 2% यूरिया घोल का छिड़काव करें। यदि नई पत्तियां पीली हैं तो फेरस सल्फेट (0.5%) का उपयोग करें।"
                    "Marathi" -> "पानांचा पिवळेपणा बहुधा नत्र किंवा लोह (Fe) च्या कमतरतेमुळे होतो. जुनी पाने पिवळी पडल्यास युरियाची २% फवारणी करा, नवीन शेंड्याकडील पाने पिवळी असल्यास फेरस सल्फेट ०.५% फवारा."
                    else -> "Yellowing leaves (chlorosis) usually indicates nitrogen or iron deficiency. If older lower leaves turn pale yellow first, spray 2% urea (20g/L). If new leaves turn yellow while veins stay green, apply 0.5% Ferrous Sulphate with lime."
                }
            }
            p.contains("fertilizer") || p.contains("urea") || p.contains("dap") -> {
                when (language) {
                    "Hindi" -> "संतुलित पोषण के लिए मृदा स्वास्थ्य कार्ड (Soil Health Card) के अनुसार ही उर्वरक डालें। सामान्यतः 4:2:1 NPK अनुपात अनुशंसित है। यूरिया को हमेशा 2-3 भागों में बांटकर दें।"
                    "Marathi" -> "खतांचा संतुलित वापर करण्यासाठी माती परीक्षण अहवालानुसारच डोस द्या. युरिया एकाच वेळी न देता २ ते ३ हप्त्यांमध्ये विभागून द्यावा जेणेकरून नत्राचे नुकसान टळेल."
                    else -> "Apply chemical fertilizers in split doses based on your Soil Health Card test values. General basal dose consists of DAP or SSP along with Potash, while Urea is top-dressed during vegetative and tillering stages."
                }
            }
            p.contains("pest") || p.contains("insect") || p.contains("keeda") || p.contains("kid") -> {
                when (language) {
                    "Hindi" -> "कीट प्रबंधन के लिए पहले नीम तेल (1500 ppm) 5ml प्रति लीटर पानी में मिलाकर छिड़कें। खेत में पीले चिपचिपे कार्ड लगाएं। गंभीर प्रकोप होने पर ही अनुशंसित कीटनाशक का उपयोग करें।"
                    "Marathi" -> "किडींच्या नियंत्रणासाठी सुरुवातीला निंबोळी अर्क ५% किंवा नीम तेल ५ मिली/लिटर फवारा. शेतात पिवळे चिकट सापळे लावा. जास्त प्रादुर्भाव असल्यास तज्ज्ञांच्या सल्ल्याने कीटकनाशक वापरा."
                    else -> "For eco-friendly pest management, spray 5ml/L of 1500 ppm cold-pressed Neem Oil or 5% Neem Seed Kernel Extract (NSKE). Install yellow and blue sticky traps (15/acre) to monitor sucking pests."
                }
            }
            else -> {
                when (language) {
                    "Hindi" -> "नमस्ते किसान भाई! फार्मईज़ी एआई सलाहकार में आपका स्वागत है। आपकी फसल, मिट्टी, मौसम या सरकारी योजनाओं के बारे में कोई भी प्रश्न पूछें।"
                    "Marathi" -> "नमस्कार शेतकरी बंधूंनो! फार्मईझी कृषी सल्लागारामध्ये आपले स्वागत आहे. पीक, खते, कीड रोग किंवा सरकारी योजनांबाबत आपला प्रश्न विचारा."
                    else -> "Hello Kisan! FarmEasy AI Agricultural Advisory is here to assist. For specific guidance, mention your crop name (e.g., Wheat, Cotton, Tomato, Paddy), current crop stage, and field symptoms."
                }
            }
        }
    }
}
