package ru.bash.commands.impl

import ru.bash.Shell
import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths

class CdCommand : Command {
    override val name = "cd"

    override fun execute(
        argv: List<String>,
        stdin: InputStream,
        stdout: OutputStream,
        stderr: OutputStream,
        environment: Shell.ShellEnvironment
    ): Int {
        val args = argv.drop(1)
        val newPath = when {
            args.isEmpty() -> Paths.get(System.getProperty("user.home"))
            else -> Paths.get(args[0])
        }
        val resolved = if (newPath.isAbsolute) {
            newPath.normalize()
        } else {
            environment.currentWorkingDirectory.resolve(newPath).normalize()
        }
        try {
            if (Files.isDirectory(resolved)) {
                environment.currentWorkingDirectory = resolved
                return 0
            } else if (Files.isRegularFile(resolved)) {
                stderr.write("cd: Not a directory: ${args[0]}\n".toByteArray())

            } else {
                stderr.write("cd: No such file or directory: ${args[0]}\n".toByteArray())

            }
        } catch (e: SecurityException) {
            stderr.write("cd: Permission denied: ${e.message}\n".toByteArray())
        } catch (e: IllegalArgumentException) {
            stderr.write("cd: Invalid path: ${e.message}\n".toByteArray())
        }
        return 1
    }
}
