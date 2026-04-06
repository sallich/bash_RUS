package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class ExternalCommand(
    override val name: String
) : Command {

    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream): Int {
        val process = ProcessBuilder(argv)
            .redirectErrorStream(true)
            .start()

        val pipeIn = Thread {
            stdin.copyTo(process.outputStream)
            process.outputStream.close()
        }
        val pipeOut = Thread {
            process.inputStream.copyTo(stdout)
        }

        pipeIn.start()
        pipeOut.start()

        val exitCode = process.waitFor()
        pipeIn.join()
        pipeOut.join()

        return exitCode
    }
}
