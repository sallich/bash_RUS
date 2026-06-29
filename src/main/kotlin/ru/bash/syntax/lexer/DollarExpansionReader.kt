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
            c == '?' -> readExitStatus(dollarPos)
            c == '(' -> {
                readParenAfterDollar(dollarPos)
                true
            }
            c == '{' -> readBracedVar(dollarPos)
            c.isLetterOrDigit() || c == '_' -> readSimpleVar(dollarPos)
            else -> false
        }
    }

    private fun readExitStatus(dollarPos: Int): Boolean {
        stream.next()
        addToken(
            TokenType.EXIT_STATUS,
            "",
            dollarPos,
            stream.position()
        )
        return true
    }

    private fun readParenAfterDollar(dollarPos: Int) {
        stream.next()
        val next = stream.peek()
        if (next == '(') {
            stream.next()
            val expr = readArithmeticExpression(dollarPos)
            addToken(
                TokenType.ARITHMETIC_EXPANSION,
                expr,
                dollarPos,
                stream.position()
            )
        } else {
            val inner = readCommandSubstitutionInner(dollarPos)
            addToken(
                TokenType.COMMAND_SUBSTITUTION,
                inner,
                dollarPos,
                stream.position()
            )
        }
    }

    private fun readArithmeticExpression(dollarPos: Int): String {
        val sb = StringBuilder()
        var depth = 0
        while (true) {
            val c = stream.peek() ?: throw ParseException("Unterminated \"\$((...))\"", dollarPos)
            when {
                c == '(' -> {
                    depth++
                    sb.append(stream.next()!!)
                }
                c == ')' && depth > 0 -> {
                    depth--
                    sb.append(stream.next()!!)
                }
                c == ')' -> {
                    stream.next()
                    val n = stream.peek()
                    if (n == ')') {
                        stream.next()
                        return sb.toString()
                    }
                    throw ParseException("Unterminated \"\$((...))\"", dollarPos)
                }
                else -> sb.append(stream.next()!!)
            }
        }
    }

    private fun readCommandSubstitutionInner(dollarPos: Int): String {
        val sb = StringBuilder()
        val depth = intArrayOf(1)
        while (depth[0] > 0) {
            val c = stream.peek() ?: throw ParseException("Unterminated \"\$(...)\"", dollarPos)
            when {
                c == '\'' -> appendSingleQuotedInSubst(sb, dollarPos)
                c == '"' -> appendDoubleQuotedInSubst(sb, dollarPos, depth)
                c == ')' -> {
                    stream.next()
                    depth[0]--
                    if (depth[0] == 0) {
                        return sb.toString()
                    }
                    sb.append(')')
                }
                c == '$' -> {
                    stream.next()
                    if (stream.peek() == '(') {
                        stream.next()
                        sb.append("\$(")
                        depth[0]++
                    } else {
                        sb.append('$')
                    }
                }
                else -> sb.append(stream.next()!!)
            }
        }
        throw AssertionError("Unclosed command substitution")
    }

    private fun appendSingleQuotedInSubst(sb: StringBuilder, dollarPos: Int) {
        sb.append(stream.next()!!)
        while (true) {
            val c = stream.peek() ?: throw ParseException(
                "Unterminated single quote in command substitution",
                dollarPos
            )
            sb.append(stream.next()!!)
            if (c == '\'') return
        }
    }

    private fun appendDoubleQuotedInSubst(sb: StringBuilder, dollarPos: Int, depth: IntArray) {
        sb.append(stream.next()!!)
        while (true) {
            val c = stream.peek() ?: throw ParseException(
                "Unterminated double quote in command substitution",
                dollarPos
            )
            when (c) {
                '"' -> {
                    sb.append(stream.next()!!)
                    return
                }
                '\\' -> {
                    stream.next()
                    val n = stream.peek() ?: throw ParseException(
                        "Unterminated double quote in command substitution",
                        dollarPos
                    )
                    when (n) {
                        '"', '\\', '$', '`' -> sb.append(stream.next()!!)
                        else -> {
                            sb.append('\\')
                            sb.append(stream.next()!!)
                        }
                    }
                }
                '$' -> {
                    stream.next()
                    if (stream.peek() == '(') {
                        stream.next()
                        sb.append("\$(")
                        depth[0]++
                    } else {
                        sb.append('$')
                    }
                }
                else -> sb.append(stream.next()!!)
            }
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

        stream.next()

        if (inner.isEmpty()) {
            throw ParseException("Empty parameter expansion", dollarPos)
        }

        val name = inner.toString()

        addToken(
            TokenType.VAR,
            name,
            dollarPos,
            dollarPos + 2 + name.length + 1
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
            dollarPos + 1 + s.length 
        )

        return true
    }
}
