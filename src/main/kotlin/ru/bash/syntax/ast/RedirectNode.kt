package ru.bash.syntax.ast

sealed class RedirectNode {
    data class Out(val file: ArgumentNode, val append: Boolean) : RedirectNode()
    data class In(val file: ArgumentNode) : RedirectNode()
}
