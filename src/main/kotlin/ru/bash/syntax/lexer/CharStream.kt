package ru.bash.syntax.lexer

class CharStream (
    private val text : String
) {
    private var index = 0

    fun peek() : Char? = if (!eof()) text[index] else null

    fun next() : Char? = if (!eof()) text[index++] else null

    fun eof() : Boolean = index >= text.length

    fun position() : Int = index

}