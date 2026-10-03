package com.movtery.guide

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.unit.IntSize

@SuppressLint("ModifierNodeInspectableProperties")
internal class ShareSiblingsInputElement : ModifierNodeElement<ShareSiblingsNode>() {
    override fun create(): ShareSiblingsNode = ShareSiblingsNode()
    override fun update(node: ShareSiblingsNode) {}
    override fun equals(other: Any?): Boolean = other is ShareSiblingsInputElement
    override fun hashCode(): Int = ShareSiblingsNode::class.hashCode()
}

internal class ShareSiblingsNode : Modifier.Node(), PointerInputModifierNode {
    override fun sharePointerInputWithSiblings(): Boolean = true

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) = Unit

    override fun onCancelPointerInput() = Unit
}

internal fun Modifier.sharePointerInputWithSiblings(): Modifier = then(ShareSiblingsInputElement())
