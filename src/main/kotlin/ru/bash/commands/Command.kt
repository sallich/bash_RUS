package ru.bash.commands

import ru.bash.Shell
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

interface Command {
    val name: String
    val minArgs: Int get() = 0
    val maxArgs: Int get() = Int.MAX_VALUE

    fun execute(
        argv: List<String>,
        stdin: InputStream,
        stdout: OutputStream,
        stderr: OutputStream,
        environment: Shell.ShellEnvironment = Shell.ShellEnvironment(),
    ): Int

    fun validateArgs(argv: List<String>) {
        val argCount = argv.size - 1
        require(argCount >= minArgs) {
            "$name: expected at least $minArgs argument(s), got $argCount"
        }
        require(argCount <= maxArgs) {
            "$name: expected at most $maxArgs argument(s), got $argCount"
        }
    }

    fun resolve(environment: Shell.ShellEnvironment, path: String): File {
        val resolvedPath = if (Path.of(path).isAbsolute) {
            Path.of(path)
        } else {
            environment.currentWorkingDirectory.resolve(path)
        }
        return resolvedPath.toFile()
    }
}
