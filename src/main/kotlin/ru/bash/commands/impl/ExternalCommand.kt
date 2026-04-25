package ru.bash.commands.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import ru.bash.Shell
import ru.bash.commands.Command
import java.io.InputStream
import java.io.OutputStream

class ExternalCommand(
    override val name: String
) : Command {

    private fun fixDottedCommand(argv: List<String>): List<String> = when {
        argv.isEmpty() -> argv
        argv[0] == "./" && argv.size > 1 -> listOf("./" + argv[1]) + argv.drop(2)
        else -> argv
    }

    override fun execute(
        argv: List<String>,
        stdin: InputStream,
        stdout: OutputStream,
        stderr: OutputStream,
        environment: Shell.ShellEnvironment
    ): Int {
        val fixedArgv = fixDottedCommand(argv)
        val inheritStdin = stdin === System.`in`

        val builder = ProcessBuilder(fixedArgv)
        if (inheritStdin) builder.redirectInput(ProcessBuilder.Redirect.INHERIT)
        val process = builder.start()

        return runBlocking(Dispatchers.IO) {
            val pipes = buildList {
                if (!inheritStdin) add(async {
                    try { stdin.copyTo(process.outputStream) }
                    finally { process.outputStream.close() }
                })
                add(async { process.inputStream.copyTo(stdout) })
                add(async { process.errorStream.copyTo(stderr) })
            }
            val exitCode = async { process.waitFor() }
            pipes.awaitAll()
            exitCode.await()
        }
    }
}
