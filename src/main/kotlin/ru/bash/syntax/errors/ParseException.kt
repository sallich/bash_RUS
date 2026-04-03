package ru.bash.syntax.errors

class ParseException (
    message : String,
    pos : Int
) : RuntimeException("Error at position $pos: $message")
