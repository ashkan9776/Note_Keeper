package com.ahoura.notekeeper.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Basic markdown parser that handles **bold**, *italic*, and `code`.
 */
fun String.parseMarkdown(): AnnotatedString = buildAnnotatedString {
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    val italicRegex = Regex("\\*(.*?)\\*")
    val codeRegex = Regex("`(.*?)`")
    
    var lastIndex = 0
    val matches = (boldRegex.findAll(this@parseMarkdown) + 
                   italicRegex.findAll(this@parseMarkdown) + 
                   codeRegex.findAll(this@parseMarkdown))
                  .sortedBy { it.range.first }
    
    for (match in matches) {
        if (match.range.first < lastIndex) continue // Skip overlapping matches
        
        append(substring(lastIndex, match.range.first))
        
        val style = when {
            match.value.startsWith("**") -> SpanStyle(fontWeight = FontWeight.Bold)
            match.value.startsWith("*") -> SpanStyle(fontStyle = FontStyle.Italic)
            match.value.startsWith("`") -> SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            else -> SpanStyle()
        }
        
        val text = match.groupValues[1]
        withStyle(style) {
            append(text)
        }
        lastIndex = match.range.last + 1
    }
    append(substring(lastIndex))
}
