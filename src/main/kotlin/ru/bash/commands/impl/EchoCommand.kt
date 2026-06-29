package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class EchoCommand : Command {
    override val name = "echo"

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream, stderr: OutputStream): Int {
        val args = argv.drop(1)

        var trailingNewline = true
        var startIndex = 0

        if (args.isNotEmpty() && args[0] == "-n") {
            trailingNewline = false
            startIndex = 1
        }

        val output = args.subList(startIndex, args.size).joinToString(" ")
        stdout.write(output.toByteArray())
        if (trailingNewline) {
            stdout.write('\n'.code)
        }
        stdout.flush()
        return 0
    }
}
