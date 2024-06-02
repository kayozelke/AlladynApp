package com.example.alladyn.datasource

import com.example.alladyn.model.CarpetModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DatabaseReference

class DatabaseHandler {
    // create a FirebaseDatabase instance with the Firebase.database() method.
    // The argument of this method is the path to the database.
    private val firebaseDatabase by lazy { Firebase.database("https://alladyn-91bcc-default-rtdb.europe-west1.firebasedatabase.app/") }

    fun getDatabaseReference(): DatabaseReference {
        // Get the database reference for all-users level
        return firebaseDatabase.reference
    }

}