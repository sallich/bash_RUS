package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class ExternalCommand(
    override val name: String
) : Command {

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream, stderr: OutputStream): Int {
        val inheritStdin = stdin === System.`in`

        val builder = ProcessBuilder(argv)
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
