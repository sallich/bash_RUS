package ru.bash.syntax.ast

data class ShellWordNode(val parts: List<ArgumentNode>) : ArgumentNode {
    init {
        require(parts.isNotEmpty()) { "ShellWordNode must contain at least one part" }
    }
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitShellWord(this)
}
