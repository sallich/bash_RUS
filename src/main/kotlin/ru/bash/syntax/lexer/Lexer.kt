package ru.bash.syntax.lexer

import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.Token
import ru.bash.syntax.token.TokenType

class Lexer(
    input: String,
) {
    private val stream = CharStream(input)
    private val tokens = mutableListOf<Token>()
    private var state = LexerState.NORMAL

    private val dollarReader = DollarExpansionReader(stream, ::addToken)
    private val doubleQuoteTokenizer = DoubleQuoteTokenizer(
        stream = stream,
        tokens = tokens,
        addToken = ::addToken,
        tryReadVar = dollarReader::tryReadParameterExpansionAfterDollar
    )

    fun tokenize(): List<Token> {
        while (!stream.eof()) {
            when (state) {
                LexerState.NORMAL -> tokenizeNormal()
                LexerState.SINGLE_QUOTED -> tokenizeQuoted(stream.position())
                LexerState.DOUBLE_QUOTED -> tokenizeDoubleQuoted()
            }
        }
        val eofPos = stream.position()
        addToken(TokenType.EOF, "", eofPos, eofPos)
        return tokens
    }

    private fun tokenizeQuoted(start: Int) {
        val sb = StringBuilder()
        var c = stream.next()
        while (c != null && c != '\'') {
            sb.append(c)
            c = stream.next()
        }
        if (c != '\'') {
            throw ParseException("Unterminated quote starting", start)
        }
        addToken(
            TokenType.SINGLE_QUOTED,
            sb.toString(),
            start,
            stream.position()
        )
    }

    private fun tokenizeDoubleQuoted() {
        doubleQuoteTokenizer.tokenize {
            state = LexerState.NORMAL
        }
    }

    private fun tokenizeNormal() {
        val c = stream.peek() ?: return

        when {
            c.isWhitespace() -> stream.next()
            c == '|' -> simpleToken(TokenType.PIPELINE, "|")
            c == '>' -> simpleToken(TokenType.REDIRECT_OUT, ">")
            c == '<' -> simpleToken(TokenType.REDIRECT_IN, "<")
            c == '\'' -> {
                val start = stream.position()
                stream.next()
                tokenizeQuoted(start)
            }
            c == '"' -> {
                stream.next()
                state = LexerState.DOUBLE_QUOTED
            }
            c == '$' -> tokenizeDollarNormal()
            else -> tokenizeWord()
        }
    }

    private fun simpleToken(type: TokenType, text: String) {
        val p = stream.position()
        stream.next()
        addToken(type, text, p, p + 1)
    }

    private fun tokenizeDollarNormal() {
        val start = stream.position()
        stream.next()
        if (!dollarReader.tryReadParameterExpansionAfterDollar(start)) {
            addToken(TokenType.WORD, "$", start, start + 1)
        }
    }

    private fun tokenizeWord() {
        val start = stream.position()
        val sb = StringBuilder()

        var c = stream.peek()
        while (c != null && !c.isWhitespace() && !isSpecial(c)) {
            sb.append(stream.next()!!)
            c = stream.peek()
        }

        val text = sb.toString()
        addToken(TokenType.WORD, text, start, start + text.length)
    }

    private fun addToken(type: TokenType, text: String, position: Int, endExclusive: Int) {
        tokens += Token(type, text, position, endExclusive)
    }

    private fun isSpecial(c: Char): Boolean {
        return c in setOf('|', '>', '<', '$', '\'', '"')
    }
}
