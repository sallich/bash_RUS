package ru.bash.syntax.lexer

import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.Token
import ru.bash.syntax.token.TokenType

class DoubleQuoteTokenizer(
    private val stream: CharStream,
    private val tokens: MutableList<Token>,
    private val addToken: (TokenType, String, Int, Int) -> Unit,
    private val tryReadVar: (Int) -> Boolean
) {

    private data class State(
        val contentStart: Int,
        var partStart: Int,
        val sb: StringBuilder = StringBuilder(),
        var produced: Boolean = false,
        val firstTokenIndex: Int
    )

    fun tokenize(onFinish: () -> Unit) {
        val s = State(
            contentStart = stream.position(),
            partStart = stream.position(),
            firstTokenIndex = tokens.size
        )

        while (true) {
            val c = stream.peek() ?: unterminated(s)

            when (c) {
                '"'  -> return finish(s, onFinish)
                '\\' -> handleEscape(s)
                '$'  -> handleDollar(s)
                else -> handleChar(s)
            }
        }
    }

    private fun finish(s: State, onFinish: () -> Unit) {
        stream.next()
        val end = stream.position()

        flush(s)

        if (!s.produced) {
            addToken(
                TokenType.DOUBLE_QUOTED_LITERAL,
                "",
                s.contentStart,
                s.contentStart
            )
        }

        patchLastToken(s, end)
        onFinish()
    }

    private fun handleEscape(s: State) {
        stream.next()
        val n = stream.next() ?: unterminated(s)

        when (n) {
            '"', '\\', '$', '`' -> s.sb.append(n)
            '\n' -> throw ParseException("Line continuation not supported", stream.position())
            else -> {
                s.sb.append('\\')
                s.sb.append(n)
            }
        }
    }

    private fun handleDollar(s: State) {
        flush(s)

        val pos = stream.position()
        stream.next()

        if (tryReadVar(pos)) {
            s.produced = true
        } else {
            if (s.sb.isEmpty()) s.partStart = pos
            s.sb.append('$')
        }
    }

    private fun handleChar(s: State) {
        s.sb.append(stream.next()!!)
    }

    private fun flush(s: State) {
        if (s.sb.isNotEmpty()) {
            val text = s.sb.toString()
            addToken(
                TokenType.DOUBLE_QUOTED_LITERAL,
                text,
                s.partStart,
                s.partStart + text.length
            )
            s.produced = true
            s.sb.clear()
        }
        s.partStart = stream.position()
    }

    private fun patchLastToken(s: State, end: Int) {
        if (tokens.size > s.firstTokenIndex) {
            val i = tokens.lastIndex
            tokens[i] = tokens[i].copy(endExclusive = end)
        }
    }

    private fun unterminated(s: State): Nothing {
        throw ParseException("Unterminated double quote", s.partStart)
    }
}
