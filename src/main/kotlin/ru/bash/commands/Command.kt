package ru.bash.commands

import java.io.InputStream
import java.io.OutputStream

interface Command {
    val name: String
    val minArgs: Int get() = 0
    val maxArgs: Int get() = Int.MAX_VALUE

    fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream, stderr: OutputStream): Int

    fun validateArgs(argv: List<String>) {
        val argCount = argv.size - 1
        require(argCount >= minArgs) {
            "$name: expected at least $minArgs argument(s), got $argCount"
        }
        require(argCount <= maxArgs) {
            "$name: expected at most $maxArgs argument(s), got $argCount"
        }
    }
}
