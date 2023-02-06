package ru.netology.nmedia.ui.decor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter

class CustomItemDecorator (context: Context) : RecyclerView.ItemDecoration() {

    private val decorationToday = ContextCompat.getDrawable(context, R.drawable.item_decoration_today)
    private val decorationYesterday = ContextCompat.getDrawable(context, R.drawable.item_decoration_yesterday)
    private val decorationLastWeek = ContextCompat.getDrawable(context, R.drawable.item_decoration_last_week)

    override fun getItemOffsets(rect: Rect, view: View, parent: RecyclerView, s: RecyclerView.State) {
        parent.adapter?.let { adapter ->
            val childAdapterPosition = parent.getChildAdapterPosition(view)
                .let { if (it == RecyclerView.NO_POSITION) return else it }

            rect.right = when (adapter.getItemViewType(childAdapterPosition)) {
                PostsAdapter.TODAY_POST_ID -> decorationToday?.intrinsicWidth ?: return
                PostsAdapter.YESTERDAY_POST_ID -> decorationYesterday?.intrinsicWidth ?: return
                PostsAdapter.LAST_WEEK_POST_ID -> decorationLastWeek?.intrinsicWidth ?: return
                else -> 0
            }
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.adapter?.let { adapter ->
            parent.children
                .forEach { view ->
                    val childAdapterPosition = parent.getChildAdapterPosition(view)
                        .let { if (it == RecyclerView.NO_POSITION) return else it }

                    when (adapter.getItemViewType(childAdapterPosition)) {
                        PostsAdapter.TODAY_POST_ID -> decorationToday?.drawSeparator(view, parent, canvas) ?: return
                        PostsAdapter.YESTERDAY_POST_ID -> decorationYesterday?.drawSeparator(view, parent, canvas) ?: return
                        PostsAdapter.LAST_WEEK_POST_ID -> decorationLastWeek?.drawSeparator(view, parent, canvas) ?: return
                        else -> return
                    }
                }
        }
    }

    private fun Drawable.drawSeparator(view: View, parent: RecyclerView, canvas: Canvas) =
        apply {
            val left = view.right
            val top = parent.paddingTop
            val right = left + intrinsicWidth
            val bottom = top + intrinsicHeight - parent.paddingBottom
            bounds = Rect(left, top, right, bottom)
            draw(canvas)
        }

}