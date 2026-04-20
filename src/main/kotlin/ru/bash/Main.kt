<<<<<<< cppteam_cd_command
package ru.bash

import ru.bash.commands.impl.CatCommand
import ru.bash.commands.impl.CdCommand
import ru.bash.commands.impl.CommandRegistryImpl
import ru.bash.commands.impl.EchoCommand
import ru.bash.commands.impl.ExitCommand
import ru.bash.commands.impl.PwdCommand
import ru.bash.commands.impl.GrepCommand
import ru.bash.commands.impl.WcCommand
import ru.bash.commands.impl.LsCommand
import ru.bash.executor.PipelineExecutor

fun main() {
    val registry = CommandRegistryImpl(
        listOf(EchoCommand(), PwdCommand(), CatCommand(), ExitCommand(), GrepCommand(), WcCommand(),
            LsCommand(), CdCommand())
    )
    val executor = PipelineExecutor(registry)
    Shell(executor).run()
}
=======
package ru.bash

import ru.bash.commands.impl.CatCommand
import ru.bash.commands.impl.CommandRegistryImpl
import ru.bash.commands.impl.EchoCommand
import ru.bash.commands.impl.ExitCommand
import ru.bash.commands.impl.PwdCommand
import ru.bash.commands.impl.GrepCommand
import ru.bash.commands.impl.WcCommand
import ru.bash.commands.impl.LsCommand
import ru.bash.executor.PipelineExecutor

fun main() {
    val registry = CommandRegistryImpl(
        listOf(EchoCommand(), PwdCommand(), CatCommand(), ExitCommand(), GrepCommand(), WcCommand(), LsCommand())
    )
    val executor = PipelineExecutor(registry)
    Shell(executor).run()
}
>>>>>>> checkpoint3
