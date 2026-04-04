package ru.bash.syntax.ast

data class CommandNode(
    val name: String,
    val nodes: List<ArgumentNode>
) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>):
            R = visitor.visitCommand(this)
}
