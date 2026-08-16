package com.timebasedfitness.app.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = RoutineStepSerializer::class)
data class RoutineStep(
    val text: String,
    val durationSeconds: Int = 0,
    val group: String = "Tasks"
) {
    val isTimer: Boolean get() = durationSeconds > 0

    companion object {
        fun fromText(text: String, group: String = "Tasks"): RoutineStep {
            val detectedSeconds = extractDurationSeconds(text)
            return RoutineStep(text = text, durationSeconds = detectedSeconds, group = group)
        }

        fun extractDurationSeconds(text: String): Int {
            val secMatch = Regex("""\(?\b(\d+)\s*(?:s|sec|secs|seconds?)\b\)?""", RegexOption.IGNORE_CASE).find(text)
            if (secMatch != null) {
                return secMatch.groupValues[1].toIntOrNull() ?: 0
            }
            val minMatch = Regex("""\(?\b(\d+)\s*(?:m|min|mins|minutes?)\b\)?""", RegexOption.IGNORE_CASE).find(text)
            if (minMatch != null) {
                return (minMatch.groupValues[1].toIntOrNull() ?: 0) * 60
            }
            return 0
        }
    }
}

object RoutineStepSerializer : KSerializer<RoutineStep> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RoutineStep", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: RoutineStep) {
        val jsonEncoder = encoder as? kotlinx.serialization.json.JsonEncoder
        if (jsonEncoder != null) {
            if (value.durationSeconds == 0 && (value.group.isEmpty() || value.group == "Tasks")) {
                jsonEncoder.encodeJsonElement(JsonPrimitive(value.text))
            } else {
                val map = mutableMapOf<String, kotlinx.serialization.json.JsonElement>(
                    "text" to JsonPrimitive(value.text)
                )
                if (value.durationSeconds > 0) {
                    map["durationSeconds"] = JsonPrimitive(value.durationSeconds)
                }
                if (value.group.isNotEmpty() && value.group != "Tasks") {
                    map["group"] = JsonPrimitive(value.group)
                }
                jsonEncoder.encodeJsonElement(JsonObject(map))
            }
        } else {
            encoder.encodeString(value.text)
        }
    }

    override fun deserialize(decoder: Decoder): RoutineStep {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                val str = element.content
                return RoutineStep.fromText(str)
            } else if (element is JsonObject) {
                val text = element["text"]?.jsonPrimitive?.content.orEmpty()
                val duration = element["durationSeconds"]?.jsonPrimitive?.intOrNull
                    ?: RoutineStep.extractDurationSeconds(text)
                val group = element["group"]?.jsonPrimitive?.content ?: "Tasks"
                return RoutineStep(text = text, durationSeconds = duration, group = group)
            }
        }
        val text = decoder.decodeString()
        return RoutineStep.fromText(text)
    }
}
