package ru.bash.executor

data class PipelineResult(val exitCodes: List<Int>) {
    val lastExitCode: Int get() = exitCodes.lastOrNull() ?: 0
    val failed: Boolean get() = lastExitCode != 0
}
