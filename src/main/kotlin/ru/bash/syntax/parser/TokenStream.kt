package ru.bash.syntax.parser

import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.Token
import ru.bash.syntax.token.TokenType

class TokenStream(private val tokens: List<Token>) {
    private var index = 0
    fun peek() : Token = tokens.getOrElse(index) {tokens.last()}
    fun next() : Token = tokens.getOrElse(index++) {tokens.last()}
    fun match(type : TokenType) : Boolean =
        if (peek().type == type) {next(); true} else false
    fun expect(type: TokenType, message: String): Token {
        val token = peek()
        if (token.type != type) {
            throw ParseException(
                "$message, but got ${token.type}",
                token.pos
            )
        }
        return next()
    }
    fun end() : Boolean = peek().type == TokenType.EOF

}