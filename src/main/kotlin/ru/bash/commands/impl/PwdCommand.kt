package ru.bash.commands.impl

import ru.bash.Shell
import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class PwdCommand : Command {
    override val name = "pwd"
    override val maxArgs = 0

    override fun execute(
        argv: List<String>,
        stdin: InputStream,
        stdout: OutputStream,
        stderr: OutputStream,
        environment: Shell.ShellEnvironment
    ): Int {
        validateArgs(argv)
        val cwd = environment.currentWorkingDirectory.toAbsolutePath().toString()
        stdout.write("$cwd\n".toByteArray())
        stdout.flush()
        return 0
    }
}
