package ru.bash.semantic

fun interface CommandSubstitutionRunner {
    suspend fun runSubstitution(inner: String): String
}
