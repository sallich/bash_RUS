package ru.bash.syntax.ast

data object ExitStatusNode : ArgumentNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitExitStatus(this)
}
