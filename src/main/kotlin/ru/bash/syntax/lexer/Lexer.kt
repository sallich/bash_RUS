package ru.bash.syntax.lexer

import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.Token
import ru.bash.syntax.token.TokenType

class Lexer (
    input : String
) {
    private val stream = CharStream(input)
    private val tokens = mutableListOf<Token>()
    private var state = LexerState.NORMAL

    fun tokenize() : List<Token> {
        while(!stream.eof()) {
            when(state) {
               LexerState.NORMAL -> tokenizeNormal()
               LexerState.SINGLE_QUOTED -> tokenizeQuoted('\'', TokenType.SINGLE_QUOTED)
               LexerState.DOUBLE_QUOTED -> tokenizeQuoted('"', TokenType.STRING)
            }
        }
        addToken(TokenType.EOF, "")
        return tokens
    }

    private fun tokenizeQuoted(endChar: Char, type: TokenType) {
        val start = stream.position()
        val sb = StringBuilder()
        var c = stream.next()
        while (c != null && c != endChar) {
            sb.append(c)
            c = stream.next()
        }
        if (c != endChar) {
            throw ParseException("Unterminated quote starting", start)
        }
        state = LexerState.NORMAL
        addToken(type, sb.toString(), start)
    }

    private fun tokenizeNormal() {
        val c = stream.peek() ?: return
        when {
            c.isWhitespace() -> stream.next()
            c == '|' -> addToken(TokenType.PIPELINE, stream.next().toString())
            c == '>' -> addToken(TokenType.REDIRECT_OUT, stream.next().toString())
            c == '<' -> addToken(TokenType.REDIRECT_IN, stream.next().toString())
            c == '\'' -> {
                stream.next()
                state = LexerState.SINGLE_QUOTED
            }
            c == '"' -> {
                stream.next()
                state = LexerState.DOUBLE_QUOTED
            }
            c == '$' -> tokenizeVar()
            else -> tokenizeWord()
        }
    }

    private fun tokenizeVar() {
        val start = stream.position()
        stream.next()
        val name = StringBuilder()
        var c = stream.peek()
        while (c != null && (c.isLetterOrDigit() || c == '_')) {
            name.append(stream.next())
            c = stream.peek()
        }
        addToken(TokenType.VAR, name.toString(), start)
    }

    private fun tokenizeWord() {
        val start = stream.position()
        val s = StringBuilder()
        var c = stream.peek()
        while (c != null && !c.isWhitespace() && !isSpecial(c)) {
            s.append(stream.next())
            c = stream.peek()
        }
        addToken(TokenType.WORD, s.toString(), start)
    }

    private fun addToken(type: TokenType, text: String, position: Int = stream.position()) {
        tokens += Token(type, text, position)
    }

    private fun isSpecial(c: Char): Boolean {
        return c in setOf('|', '>', '<', '$', '\'', '"')
    }
}
