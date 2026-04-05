package ru.bash.syntax.ast

data class WordNode(val value: String) : ArgumentNode {
    override fun <R> accept(visitor: AstVisitor<R>):
            R = visitor.visitWord(this)
}
