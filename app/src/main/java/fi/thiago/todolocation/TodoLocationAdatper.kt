package fi.thiago.todolocation

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoLocationAdatper(private val todoList: MutableList<TodoModel>) :
    RecyclerView.Adapter<TodoLocationAdatper.todoViewHolder>() {

    inner class todoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView = view.findViewById<TextView>(R.id.location_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): todoViewHolder {
        var v: View? = null
        v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, null, false)
        return v?.let { todoViewHolder(it) }!!
    }

    override fun onBindViewHolder(holder: todoViewHolder, position: Int) {
        val todoObject = todoList[position]
        holder.textView.text = todoObject.title
        holder.textView.setOnClickListener {
            Intent(it.context, ItemsActivity::class.java).apply {
                this.putExtra("position",position)
                this.putExtra("id",todoObject.id)
                it.context.startActivity(this)
            }
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }
}