package ca.uwaterloo.atlas.ui.utils

import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

fun parseMarkdown(input: String): AnnotatedString {

    return buildAnnotatedString {

        val lines = input.split("\n")

        for (line in lines) {

            when {

                // Heading 1
                line.startsWith("# ") -> {
                    appendStyledHeading(
                        text = line.removePrefix("# "),
                        fontSizeSp = 26,
                        color = Color(0xFF1A237E)
                    )
                }

                // Heading 2
                line.startsWith("## ") -> {
                    appendStyledHeading(
                        text = line.removePrefix("## "),
                        fontSizeSp = 22,
                        color = Color(0xFF283593)
                    )
                }

                // Heading 3
                line.startsWith("### ") -> {
                    appendStyledHeading(
                        text = line.removePrefix("### "),
                        fontSizeSp = 18,
                        color = Color(0xFF3949AB)
                    )
                }

                // Horizontal Rule
                line.trim() == "---" -> {
                    append("\n──────────────\n")
                }

                // Bullet List
                line.startsWith("- ") || line.startsWith("* ") -> {
                    append("• ")
                    appendInlineStyles(line.substring(2))
                }

                // Numbered List
                Regex("^\\d+\\. ").matches(line.takeWhile { it != ' ' } + " ") -> {
                    append(line.substringBefore(" ") + " ")
                    appendInlineStyles(line.substringAfter(" "))
                }

                else -> {
                    appendInlineStyles(line)
                }
            }

            append("\n")
        }
    }
}

/* ---------- Heading Helpers ---------- */

private fun AnnotatedString.Builder.appendStyledHeading(
    text: String,
    fontSizeSp: Int,
    color: Color
) {
    withStyle(
        SpanStyle(
            fontWeight = FontWeight.Bold,
            fontSize = fontSizeSp.sp,
            color = color
        )
    ) {
        append(text)
    }
}

/* ---------- Inline Parsing ---------- */

private fun AnnotatedString.Builder.appendInlineStyles(text: String) {

    var index = 0

    while (index < text.length) {

        // Link [text](url)
        if (text.startsWith("[", index)) {
            val closeBracket = text.indexOf("]", index)
            val openParen = text.indexOf("(", closeBracket)
            val closeParen = text.indexOf(")", openParen)

            if (closeBracket != -1 && openParen != -1 && closeParen != -1) {
                val linkText = text.substring(index + 1, closeBracket)
                val url = text.substring(openParen + 1, closeParen)

                pushStringAnnotation(
                    tag = "URL",
                    annotation = url
                )
                withStyle(
                    SpanStyle(
                        color = Color(0xFF1E88E5),
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(linkText)
                }
                pop()

                index = closeParen + 1
                continue
            }
        }

        // Bold **text**
        if (text.startsWith("**", index)) {
            val end = text.indexOf("**", index + 2)
            if (end != -1) {
                val boldText = text.substring(index + 2, end)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldText)
                }
                index = end + 2
                continue
            }
        }

        // Italic *text*
        if (text.startsWith("*", index)) {
            val end = text.indexOf("*", index + 1)
            if (end != -1) {
                val italicText = text.substring(index + 1, end)
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicText)
                }
                index = end + 1
                continue
            }
        }

        // Underline __text__
        if (text.startsWith("__", index)) {
            val end = text.indexOf("__", index + 2)
            if (end != -1) {
                val underText = text.substring(index + 2, end)
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(underText)
                }
                index = end + 2
                continue
            }
        }

        append(text[index])
        index++
    }
}