package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class ExternalCommand(
    override val name: String
) : Command {

    private fun fixDottedCommand(argv: List<String>): List<String> {
        val result = when {
            argv.isEmpty() -> argv
            argv[0] == "./" && argv.size > 1 ->
                listOf("./" + argv[1]) + argv.drop(2)
            else -> argv
        }
        return result
    }

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream, stderr: OutputStream): Int {
        val fixedArgv = fixDottedCommand(argv)
        val inheritStdin = stdin === System.`in`

        val builder = ProcessBuilder(fixedArgv)
        if (inheritStdin) {
            builder.redirectInput(ProcessBuilder.Redirect.INHERIT)
        }
        val process = builder.start()

        val pipeIn = if (!inheritStdin) Thread {
            try {
                stdin.copyTo(process.outputStream)
            } finally {
                process.outputStream.close()
            }
        } else null

        val pipeOut = Thread { process.inputStream.copyTo(stdout) }
        val pipeErr = Thread { process.errorStream.copyTo(stderr) }

        pipeIn?.start()
        pipeOut.start()
        pipeErr.start()

        val exitCode = process.waitFor()
        pipeIn?.join()
        pipeOut.join()
        pipeErr.join()

        return exitCode
    }
}
