package fi.thiago.todolocation

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class TodoLocationAdatper(private val todoList: MutableList<TodoModel>) :
    RecyclerView.Adapter<TodoLocationAdatper.todoViewHolder>() {

    inner class todoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView = view.findViewById<TextView>(R.id.location_title)
        var delete = view.findViewById<ImageView>(R.id.delete_loc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): todoViewHolder {
        val v: View? = LayoutInflater.from(parent.context).inflate(R.layout.item_row, null, false)
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
        holder.delete.setOnClickListener { view ->
            val dd = Firebase.firestore
            val builder = AlertDialog.Builder(view.context)
            builder.setMessage("Are you sure you want to Delete ${todoObject.title}?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    todoObject.id?.let { it1 ->
                        dd.collection("todoList").document(it1)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(view.context, "Delete", Toast.LENGTH_SHORT).show()
                                todoList.removeAt(holder.adapterPosition)
                                notifyItemRemoved(holder.adapterPosition)
                                notifyItemRangeChanged(holder.adapterPosition, todoList.size)
                                Log.d("TAG", "DocumentSnapshot successfully deleted!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }
                    }
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }
}