package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class CatCommand : Command {
    override val name = "cat"

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream, stderr: OutputStream): Int {
        val files = argv.drop(1)

        if (files.isEmpty()) {
            stdin.copyTo(stdout)
            stdout.flush()
            return 0
        }

        var exitCode = 0
        for (path in files) {
            val file = File(path)
            if (!file.exists()) {
                stderr.write("cat: $path: No such file or directory\n".toByteArray())
                stderr.flush()
                exitCode = 1
                continue
            }
            file.inputStream().use { it.copyTo(stdout) }
        }
        stdout.flush()
        return exitCode
    }
}
