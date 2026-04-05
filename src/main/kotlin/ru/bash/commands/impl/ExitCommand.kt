package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class ExitCommand : Command {
    override val name = "exit"
    override val maxArgs: Int = 1

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream): Int {
        validateArgs(argv)
        val code = if (argv.size > 1) {
            argv[1].toIntOrNull()
                ?: throw IllegalArgumentException("exit: ${argv[1]}: numeric argument required")
        } else {
            0
        }
        throw ShellExitException(code)
    }
}

class ShellExitException(val code: Int) : RuntimeException("exit $code")
