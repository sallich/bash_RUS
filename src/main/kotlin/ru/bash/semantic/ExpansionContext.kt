package ru.bash.semantic

data class ExpansionContext(
    val environment: Map<String, String>,
    val lastExitCode: Int,
)
