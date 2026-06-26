package com.ahoura.notekeeper.data.repository

import com.ahoura.notekeeper.data.local.entity.NoteEntity
import com.ahoura.notekeeper.domain.model.ChecklistItem
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.model.NoteColor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Translation between the Room [NoteEntity] and the domain [Note]. Kept free of the
 * serialization compiler plugin by using explicit built-in / hand-written serializers for the
 * label and checklist lists.
 */

private val labelsSerializer = ListSerializer(String.serializer())

/** Hand-written serializer for [ChecklistItem] so we avoid the @Serializable compiler plugin. */
private object ChecklistItemSerializer : KSerializer<ChecklistItem> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("ChecklistItem") {
            element<String>("text")
            element<Boolean>("isChecked")
        }

    override fun serialize(encoder: Encoder, value: ChecklistItem) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.text)
            encodeBooleanElement(descriptor, 1, value.isChecked)
        }
    }

    override fun deserialize(decoder: Decoder): ChecklistItem =
        decoder.decodeStructure(descriptor) {
            var text = ""
            var isChecked = false
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> text = decodeStringElement(descriptor, 0)
                    1 -> isChecked = decodeBooleanElement(descriptor, 1)
                    else -> break
                }
            }
            ChecklistItem(text, isChecked)
        }
}

private val checklistSerializer = ListSerializer(ChecklistItemSerializer)
private val zone: ZoneId = ZoneId.systemDefault()

private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zone)

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(zone).toInstant().toEpochMilli()

private fun encodeLabels(labels: List<String>): String =
    if (labels.isEmpty()) "[]" else Json.encodeToString(labelsSerializer, labels)

private fun decodeLabels(json: String): List<String> =
    if (json.isBlank()) emptyList()
    else runCatching { Json.decodeFromString(labelsSerializer, json) }.getOrDefault(emptyList())

private fun encodeChecklist(items: List<ChecklistItem>): String =
    if (items.isEmpty()) "[]" else Json.encodeToString(checklistSerializer, items)

private fun decodeChecklist(json: String): List<ChecklistItem> =
    if (json.isBlank()) emptyList()
    else runCatching { Json.decodeFromString(checklistSerializer, json) }.getOrDefault(emptyList())

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    color = NoteColor.fromHex(colorHex),
    isPinned = isPinned,
    isArchived = isArchived,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    labels = decodeLabels(labelsJson),
    reminderAt = reminderAt?.toLocalDateTime(),
    isChecklist = isChecklist,
    checklistItems = decodeChecklist(checklistJson),
    isTrashed = isTrashed,
    trashedAt = trashedAt?.toLocalDateTime()
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    colorHex = color.hexValue,
    isPinned = isPinned,
    isArchived = isArchived,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    labelsJson = encodeLabels(labels),
    reminderAt = reminderAt?.toEpochMillis(),
    isChecklist = isChecklist,
    checklistJson = encodeChecklist(checklistItems),
    isTrashed = isTrashed,
    trashedAt = trashedAt?.toEpochMillis()
)
