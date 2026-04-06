package ru.bash.syntax.token

enum class TokenType {
    WORD,
    PIPELINE,
    VAR,
    SINGLE_QUOTED,
    DOUBLE_QUOTED_LITERAL,
    REDIRECT_IN,
    REDIRECT_OUT,
    EOF
}
