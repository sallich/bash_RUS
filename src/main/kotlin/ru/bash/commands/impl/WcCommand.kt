package ru.bash.commands.impl

import ru.bash.Shell
import ru.bash.commands.Command
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class WcCommand : Command {
    override val name = "wc"

    override fun execute(
        argv: List<String>,
        stdin: InputStream,
        stdout: OutputStream,
        stderr: OutputStream,
        environment: Shell.ShellEnvironment
    ): Int {
        val (options, files) = parseArguments(argv)
        val showAll = !options.lines && !options.words && !options.bytes

        if (files.isEmpty()) {
            val counts = count(stdin)
            stdout.write((formatCounts(counts, showAll, options, null) + "\n").toByteArray())
            stdout.flush()
            return 0
        }

        var exitCode = 0
        var totalLines = 0L
        var totalWords = 0L
        var totalBytes = 0L

        for (path in files) {
            val file = resolve(environment, path)
            if (!file.exists()) {
                stderr.write("wc: $path: No such file or directory\n".toByteArray())
                exitCode = 1
                continue
            }
            val counts = file.inputStream().use { count(it) }
            totalLines += counts.lines
            totalWords += counts.words
            totalBytes += counts.bytes
            stdout.write((formatCounts(counts, showAll, options, path) + "\n").toByteArray())
        }

        if (files.size > 1) {
            val total = Counts(totalLines, totalWords, totalBytes)
            stdout.write((formatCounts(total, showAll, options, "total") + "\n").toByteArray())
        }

        stdout.flush()
        return exitCode
    }

    private fun parseArguments(argv: List<String>): Pair<Options, List<String>> {
        val files = mutableListOf<String>()
        var options = Options()

        for (arg in argv.drop(1)) {
            if (arg.startsWith("-") && arg.length > 1 && files.isEmpty()) {
                options = applyFlags(options, arg.drop(1))
            } else {
                files.add(arg)
            }
        }

        return Pair(options, files)
    }

    private fun applyFlags(options: Options, flags: String): Options {
        var result = options
        for (ch in flags) {
            result = when (ch) {
                'l' -> result.copy(lines = true)
                'w' -> result.copy(words = true)
                'c' -> result.copy(bytes = true)
                else -> result
            }
        }
        return result
    }

    private fun count(stream: InputStream): Counts {
        val content = stream.readAllBytes()
        val text = String(content)
        val lineCount = text.count { it == '\n' }.toLong()
        val wordCount = text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }.toLong()
        val byteCount = content.size.toLong()
        return Counts(lineCount, wordCount, byteCount)
    }

    private fun formatCounts(counts: Counts, showAll: Boolean, options: Options, fileName: String?): String {
        val parts = mutableListOf<String>()

        if (showAll || options.lines) parts.add("%8d".format(counts.lines))
        if (showAll || options.words) parts.add("%8d".format(counts.words))
        if (showAll || options.bytes) parts.add("%8d".format(counts.bytes))

        return parts.joinToString("") +
            if (fileName != null) " $fileName" else ""
    }

    private data class Options(
        val lines: Boolean = false,
        val words: Boolean = false,
        val bytes: Boolean = false,
    )

    private data class Counts(
        val lines: Long,
        val words: Long,
        val bytes: Long,
    )
}
