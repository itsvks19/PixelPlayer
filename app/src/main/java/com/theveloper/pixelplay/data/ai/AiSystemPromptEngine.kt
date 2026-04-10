package com.theveloper.pixelplay.data.ai


import javax.inject.Inject
import javax.inject.Singleton

enum class AiSystemPromptType {
    PLAYLIST,
    METADATA,
    TAGGING,
    MOOD_ANALYSIS,
    PERSONA,
    GENERAL
}

@Singleton
class AiSystemPromptEngine @Inject constructor() {

    // AI Optimization: Compact universal constraints to prevent common LLM "chatter" behaviors.
    // Reduced from verbose XML to minimal directives to save ~200 tokens per request.
    private val UNIVERSAL_CONSTRAINTS = """
        RULES: Output ONLY raw data. NO markdown (no ```). NO chat text. NO explanations. NO formatting chars. Raw data string ONLY. Breaking these rules crashes the parser.
    """.trimIndent()

    fun buildPrompt(basePersona: String, type: AiSystemPromptType, context: String = ""): String {
        val requirementLayer = when (type) {
            AiSystemPromptType.PLAYLIST -> """
                ROLE: Music curation engine. Select song IDs matching the user's request and listening profile.
                
                STRATEGY:
                - "new/unheard/discovery" → pick from [DISCOVERY_POOL]
                - "favorites/familiar" → pick high play-count from [LISTENED]
                - generic → blend both pools, favor top genres/artists
                - Ensure smooth flow and genre-appropriate energy progression
                
                OUTPUT: Raw JSON array of song IDs only.
                Example: ["id_123","id_456","id_789"]
            """.trimIndent()

            AiSystemPromptType.METADATA -> """
                ROLE: Metadata specialist. Return accurate, specific values for the song.
                Use specific genres (e.g. "Synthwave" not "Electronic").
                OUTPUT: Raw JSON object.
                Format: {"title":"Full Title","artist":"Primary Artist","album":"Album Name","genre":"Specific Genre"}
            """.trimIndent()

            AiSystemPromptType.TAGGING -> """
                ROLE: Generate 6-10 descriptive atmospheric tags. Lowercase, hyphenated.
                OUTPUT: Raw comma-separated list.
                Example: cinematic, orchestral, dark-ambient, hybrid-score
            """.trimIndent()

            AiSystemPromptType.MOOD_ANALYSIS -> """
                ROLE: Derive mood and energy metrics from song metadata. Floats 0.0-1.0.
                Moods: Joyful, Aggressive, Calm, Melancholic, Radiant, Intense, Somber.
                OUTPUT: Tag | Value format.
                Example: Intense | Energy:0.9 | Valence:0.1 | Danceability:0.4 | Acousticness:0.0
            """.trimIndent()

            AiSystemPromptType.PERSONA -> """
                ROLE: Poetic sonic oracle. Sophisticated, enigmatic, empathetic to user's taste.
                Reference user's specific stats to personalize. Be concise.
            """.trimIndent()

            AiSystemPromptType.GENERAL -> """
                ROLE: PixelPlayer music assistant. Help with music-related queries using library context.
            """.trimIndent()
        }

        val contextLayer = if (context.isNotBlank()) {
            """
            USER_CONTEXT:
            $context
            KEY: LISTENED=id|p(plays)|d(mins)|f(fav)|meta. DISCOVERY_POOL=unplayed tracks.
            """.trimIndent()
        } else ""

        // Token Optimization: Minimal structure, no redundant XML tags
        return """
            PERSONA: $basePersona
            $UNIVERSAL_CONSTRAINTS
            $contextLayer
            $requirementLayer
        """.trimIndent()
    }
}
