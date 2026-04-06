package ru.bash.syntax.lexer

import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.TokenType

class DollarExpansionReader(
    private val stream: CharStream,
    private val addToken: (TokenType, String, Int, Int) -> Unit
) {

    fun tryReadParameterExpansionAfterDollar(dollarPos: Int): Boolean {
        val c = stream.peek() ?: return false

        return when {
            c == '{' -> readBracedVar(dollarPos)
            c.isLetterOrDigit() || c == '_' -> readSimpleVar(dollarPos)
            else -> false
        }
    }

    private fun readBracedVar(dollarPos: Int): Boolean {
        stream.next() // consume '{'

        val inner = StringBuilder()
        var p = stream.peek()

        while (p != null && p != '}') {
            inner.append(stream.next()!!)
            p = stream.peek()
        }

        if (p != '}') {
            throw ParseException("Unterminated \"\${...}\"", dollarPos)
        }

        stream.next() // consume '}'

        if (inner.isEmpty()) {
            throw ParseException("Empty parameter expansion", dollarPos)
        }

        val name = inner.toString()

        addToken(
            TokenType.VAR,
            name,
            dollarPos,
            dollarPos + 2 + name.length + 1 // ${ + name + }
        )

        return true
    }

    private fun readSimpleVar(dollarPos: Int): Boolean {
        val name = StringBuilder()

        while (true) {
            val c = stream.peek()
            if (c == null || !(c.isLetterOrDigit() || c == '_')) break
            name.append(stream.next()!!)
        }

        val s = name.toString()

        addToken(
            TokenType.VAR,
            s,
            dollarPos,
            dollarPos + 1 + s.length // $ + name
        )

        return true
    }
}
