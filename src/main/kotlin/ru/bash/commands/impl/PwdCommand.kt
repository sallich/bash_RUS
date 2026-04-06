package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class PwdCommand : Command {
    override val name = "pwd"
    override val maxArgs = 0

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream): Int {
        validateArgs(argv)
        val cwd = System.getProperty("user.dir")
        stdout.write("$cwd\n".toByteArray())
        stdout.flush()
        return 0
    }
}
