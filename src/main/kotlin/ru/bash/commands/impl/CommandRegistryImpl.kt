package ru.bash.commands.impl

import ru.bash.commands.Command
import ru.bash.commands.CommandRegistry

class CommandRegistryImpl(
    builtins: List<Command>
) : CommandRegistry {

    private val builtinMap: Map<String, Command> = builtins.associateBy { it.name }

    override fun isBuiltin(name: String): Boolean = name in builtinMap

    override fun resolve(name: String): Command =
        builtinMap[name] ?: ExternalCommand(name)
}
