package com.garrapeta.firebasechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chatroom.*

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        private const val SIGN_IN_REQUEST_CODE = 1234
        private const val DATABASE_PATH = "chats"
        private const val TAG = "stp"
    }

    private lateinit var adapter: FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        login()
    }

    private fun login() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(),
                SIGN_IN_REQUEST_CODE
            )
        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(
                this,
                "Welcome " + (FirebaseAuth.getInstance().currentUser?.displayName
                    ?: " Unknown User"),
                Toast.LENGTH_LONG
            ).show()

            // Load chat room contents
            setupDatabaseActions()
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    this,
                    "Successfully signed in. Welcome!",
                    Toast.LENGTH_LONG
                ).show()

                setupDatabaseActions()
            } else {
                Toast.makeText(
                    this,
                    "We couldn't sign you in. Please try again later.",
                    Toast.LENGTH_LONG
                ).show()

                // Close the app
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chatroom, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                .addOnCompleteListener {
                    Toast.makeText(
                        this,
                        "You have been signed out.",
                        Toast.LENGTH_LONG
                    )
                        .show()

                    // Close activity
                    finish()
                }
        }
        return true
    }

    private fun setupDatabaseActions() {
        bindListView()
        bindSendButton()
    }

    private fun bindListView() {
        Log.i(TAG, "Binding list view")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!! + dataSnapshot.value)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException())
            }
        }
        FirebaseDatabase.getInstance()
            .reference
            .addChildEventListener(childEventListener)


        val query = FirebaseDatabase.getInstance()
            .reference
            .child(DATABASE_PATH)


        val options = FirebaseRecyclerOptions.Builder<ChatMessage>()
            .setQuery(query, ChatMessage::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = object : FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {

                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message, parent, false)

                return ChatMessageViewHolder(view)
            }

            override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int, model: ChatMessage) {

                // Set their text
                holder.messageText.text = model.messageText
                holder.messageUser.text = model.messageUser

                // Format the date before showing it
                holder.messageTime.text =
                    DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.messageTime)
            }

            override fun onDataChanged() {
                Log.i("stp", "onDataChanged. New itemCount: ${this.itemCount}")
                list_of_messages.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onError(e: DatabaseError) {
                Log.e("stp", "error $e")
                Toast.makeText(this@ChatRoomActivity, "dsa", Toast.LENGTH_SHORT).show()
            }
        }

        list_of_messages.adapter = adapter
        list_of_messages.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private fun bindSendButton() {
        fab.setOnClickListener {
            val messageText: ChatMessage? = FirebaseAuth.getInstance()
                .currentUser
                ?.displayName
                ?.let { name -> ChatMessage(input.text.toString(), name) }

            messageText?.let {
                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance().reference
                    .child(DATABASE_PATH)
                    .push()
                    .setValue(messageText)
            }

            // Clear the input
            input.setText("")
        }
    }
}
