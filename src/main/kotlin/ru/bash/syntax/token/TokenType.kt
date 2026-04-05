package ru.bash.syntax.token

enum class TokenType {
    WORD,
    PIPELINE,
    VAR,
    SINGLE_QUOTED,
    STRING,
    REDIRECT_IN,
    REDIRECT_OUT,
    EOF
}
