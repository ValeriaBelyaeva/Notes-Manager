package com.example.notes_manager.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object InstantIso8601Serializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InstantIso8601", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(DateTimeFormatter.ISO_INSTANT.format(value))
    }

    override fun deserialize(decoder: Decoder): Instant {
        val s = decoder.decodeString()
        try {
            // ISO 8601: "2025-08-15T14:23:45Z" или "2025-08-15T14:23:45.123Z"
            return Instant.parse(s)
        } catch (e: DateTimeParseException) {
            // Можно кинуть более понятную ошибку, чтобы видеть реальную кривизну данных
            throw IllegalArgumentException("Invalid ISO8601 instant: '$s'", e)
        }
    }
}
