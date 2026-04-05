package ru.bash.commands

interface CommandRegistry {
    fun isBuiltin(name: String): Boolean
    fun resolve(name: String): Command
}
