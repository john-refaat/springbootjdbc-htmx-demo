package dev.js.productsdemo.repository

import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class RepositoryUtil {
    companion object{
        fun extractTimeStampTZ(key: Any?): OffsetDateTime? = when (val v = key) {
            is OffsetDateTime -> v
            is Timestamp -> v.toInstant().atOffset(ZoneOffset.UTC) // TIMESTAMPTZ represents an instant
            is Instant -> v.atOffset(ZoneOffset.UTC)
            else -> null
        }
    }
}