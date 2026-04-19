package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class LsCommand : Command {
    override val name = "ls"

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream, stderr: OutputStream): Int {
        val paths = argv.drop(1).ifEmpty { listOf(System.getProperty("user.dir")) }
        val showHeaders = paths.size > 1

        var exitCode = 0
        paths.forEachIndexed { index, path ->
            val file = File(path)
            if (!file.exists()) {
                stderr.write("ls: cannot access '$path': No such file or directory\n".toByteArray())
                stderr.flush()
                exitCode = 1
                return@forEachIndexed
            }

            if (showHeaders) {
                if (index > 0) stdout.write("\n".toByteArray())
                stdout.write("$path:\n".toByteArray())
            }

            if (file.isDirectory) {
                file.listFiles()
                    ?.map { it.name }
                    ?.filterNot { it.startsWith(".") }
                    ?.sorted()
                    ?.forEach { stdout.write("$it\n".toByteArray()) }
            } else {
                stdout.write("$path\n".toByteArray())
            }
        }
        stdout.flush()
        return exitCode
    }
}
