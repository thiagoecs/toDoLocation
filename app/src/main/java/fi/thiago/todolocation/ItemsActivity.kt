package fi.thiago.todolocation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_items.*
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_locations.*


class ItemsActivity : AppCompatActivity() {
    private val hashMap: HashMap<String, Boolean> = HashMap()
    val todoLocationList = mutableListOf<TodoModel>()
    val db = Firebase.firestore
    val todoList = db.collection("todoList")
    var todoLocList = TodoModel()
    var position = 0
    var id = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)

        position = intent.getIntExtra("position",0)
        id = intent.getStringExtra("id").toString()
        todo_item_recyclerview.layoutManager = LinearLayoutManager(this)
        initRecyclerView()
        updateButton.setOnClickListener { btn ->
            btn.isEnabled = false

            todoLocationList[position].hashMap?.set(updateHashmap.text.toString(), false)
            val docRef = todoLocationList[position].id?.let { it1 -> db.collection("todoList").document(it1) }
            if (updateHashmap.text.isNotEmpty()){
                docRef!!.update("hashMap", todoLocationList[position].hashMap).addOnSuccessListener {
                    updateHashmap.setText("")
                    initRecyclerView()
                }
            }

        }

    }

    private fun initRecyclerView(){
        todoLocationList.clear()
        todoList
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    todoLocList = document.toObject<TodoModel>()
                    todoLocList.id = document.id
                    todoLocationList.add(todoLocList)
                }
                todo_item_recyclerview.adapter = id.let { it?.let { it1 -> todoLocationList[position].hashMap?.let { it2 ->
                    TodoItemAdapter(it1,
                        it2
                    )
                } } }
                updateButton.isEnabled = true
            }.addOnFailureListener {
                Log.e("reuslt",it.message.toString())
            }
    }
}