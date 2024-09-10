package com.example.taskmanage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var editProfileButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.profileImage)
        nameText = view.findViewById(R.id.nameText)
        emailText = view.findViewById(R.id.emailText)
        editProfileButton = view.findViewById(R.id.editProfileButton)

        // Sample data
        updateProfileInfo("John Doe", "john.doe@example.com")

        editProfileButton.setOnClickListener {
            // TODO: Implement edit profile functionality
        }
    }

    private fun updateProfileInfo(name: String, email: String) {
        nameText.text = name
        emailText.text = email
        // TODO: Update profile image
    }
}