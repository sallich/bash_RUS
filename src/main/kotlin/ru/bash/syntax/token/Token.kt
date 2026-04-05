package ru.bash.syntax.token

data class Token(
    val type: TokenType,
    val text: String,
    val pos: Int,
    val endExclusive: Int = pos + text.length,
)
