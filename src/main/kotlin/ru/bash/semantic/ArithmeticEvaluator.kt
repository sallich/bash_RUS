package ru.bash.semantic

import ru.bash.syntax.errors.ParseException

object ArithmeticEvaluator {

    fun eval(expression: String): Long {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return 0L
        val p = Parser(trimmed)
        val value = p.parseExpr()
        p.skipWs()
        if (!p.eof()) {
            throw ParseException("Unexpected character in arithmetic expansion", p.pos())
        }
        return value
    }

    private class Parser(private val s: String) {
        private var i = 0

        fun pos(): Int = i

        fun eof(): Boolean {
            skipWs()
            return i >= s.length
        }

        fun skipWs() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        fun parseExpr(): Long {
            var v = parseTerm()
            while (true) {
                skipWs()
                if (i >= s.length) return v
                when (s[i]) {
                    '+' -> {
                        i++
                        v += parseTerm()
                    }
                    '-' -> {
                        i++
                        v -= parseTerm()
                    }
                    else -> return v
                }
            }
        }

        fun parseTerm(): Long {
            var v = parseFactor()
            while (true) {
                skipWs()
                if (i >= s.length) return v
                when (s[i]) {
                    '*' -> {
                        i++
                        v *= parseFactor()
                    }
                    '/' -> {
                        i++
                        val d = parseFactor()
                        if (d == 0L) throw ParseException("Division by zero in arithmetic expansion", i)
                        v /= d
                    }
                    '%' -> {
                        i++
                        val m = parseFactor()
                        if (m == 0L) throw ParseException("Modulo by zero in arithmetic expansion", i)
                        v %= m
                    }
                    else -> return v
                }
            }
        }

        fun parseFactor(): Long {
            skipWs()
            if (i >= s.length) {
                throw ParseException("Unexpected end of arithmetic expansion", i)
            }
            return when (val c = s[i]) {
                '-' -> {
                    i++
                    -parseFactor()
                }
                '+' -> {
                    i++
                    parseFactor()
                }
                '(' -> parseParenthesizedExpr()
                else -> parseNumberStartingWithCurrentChar(c)
            }
        }

        private fun parseParenthesizedExpr(): Long {
            i++
            val v = parseExpr()
            skipWs()
            if (i >= s.length || s[i] != ')') {
                throw ParseException("Expected ')' in arithmetic expansion", i)
            }
            i++
            return v
        }

        private fun parseNumberStartingWithCurrentChar(c: Char): Long {
            if (!c.isDigit()) {
                throw ParseException("Expected number in arithmetic expansion", i)
            }
            return parseNumber()
        }

        fun parseNumber(): Long {
            skipWs()
            val start = i
            if (i >= s.length || !s[i].isDigit()) {
                throw ParseException("Expected number in arithmetic expansion", start)
            }
            while (i < s.length && s[i].isDigit()) {
                i++
            }
            return s.substring(start, i).toLong()
        }
    }
}
