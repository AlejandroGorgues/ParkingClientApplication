package com.example.parkingclientapplication.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.example.parkingclientapplication.R
import com.example.parkingclientapplication.activities.ClientMapActivity
import com.example.parkingclientapplication.interfaces.LoadFragments
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginClientFragment : Fragment() {


    private lateinit var loadFragments: LoadFragments

    private lateinit var buttonAccess: Button
    private lateinit var buttonRegister: Button

    private lateinit var edPasswordLogin: EditText
    private lateinit var edEmailLogin: EditText

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        auth = FirebaseAuth.getInstance()

        buttonAccess = view.findViewById(R.id.buttonAccess)
        buttonRegister = view.findViewById(R.id.buttonRegister)

        edEmailLogin = view.findViewById(R.id.edEmailLogin)
        edPasswordLogin = view.findViewById(R.id.edPasswordLogin)
        edEmailLogin.setText("a@gmail.com")
        edPasswordLogin.setText("123456")

        loadFragments = activity as LoadFragments

        buttonAccess.setOnClickListener {
            auth.signInWithEmailAndPassword(edEmailLogin.text.toString(), edPasswordLogin.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //Log.d(TAG, "signInWithEmail:success")
                        val intent = Intent(activity, ClientMapActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        //Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }

        buttonRegister.setOnClickListener {
            val bundle = Bundle()
            loadFragments.loadFragment(1, bundle)
        }
        // Inflate the layout for this fragment
        return view
    }
}
