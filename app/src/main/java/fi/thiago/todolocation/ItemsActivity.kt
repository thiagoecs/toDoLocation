package fi.thiago.todolocation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_items.*
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class ItemsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)
        val db = Firebase.firestore
        val todoList = db.collection("todoList")
        val todoLocationList = mutableListOf<TodoModel>()
        val position = intent.getIntExtra("position",0)
        val id = intent.getStringExtra("id")
        todo_item_recyclerview.layoutManager = LinearLayoutManager(this)
        todoLocationList.clear()
        todoList
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val todoLocList = document.toObject<TodoModel>()
                    todoLocList.id = document.id
                    todoLocationList.add(todoLocList)
                }
                todo_item_recyclerview.adapter = id.let { it?.let { it1 -> todoLocationList[position].hashMap?.let { it2 ->
                    TodoItemAdapter(it1,
                        it2
                    )
                } } }
            }.addOnFailureListener {
                Log.e("reuslt",it.message.toString())
            }

    }
}