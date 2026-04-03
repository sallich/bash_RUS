package ru.bash.syntax.errors

class ParseException (
    message : String,
    val pos : Int
) : RuntimeException(message)