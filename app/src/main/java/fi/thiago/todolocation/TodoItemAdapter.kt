package fi.thiago.todolocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TodoItemAdapter(id: String, private val todoHashMap: HashMap<String,Boolean>) :
    RecyclerView.Adapter<TodoItemAdapter.TodoItemViewHolder>() {
    private val db = Firebase.firestore
    private val docRef = db.collection("todoList").document(id)

    inner class TodoItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemCheckBox = view.findViewById<CheckBox>(R.id.item_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val v: View? =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item_row, null, false)
        return v?.let { TodoItemViewHolder(it) }!!
    }

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        val itemskeys = todoHashMap.keys.elementAt(position)
        val itemsvalue = todoHashMap.values.elementAt(position)
        holder.itemCheckBox.text = itemskeys
        holder.itemCheckBox.isChecked = itemsvalue
        holder.itemCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            todoHashMap.put(todoHashMap.keys.elementAt(position),isChecked)
            docRef.update("hashMap",todoHashMap)
        }
    }

    override fun getItemCount(): Int {
        return todoHashMap.size
    }
}