package com.example.contacts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.launch
import java.io.File

class UpdateDeleteActivity : AppCompatActivity() {

    private val vm: ContactViewModel by viewModels()
    private var currentContact: Contact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_delete)

        val contactId = intent.getIntExtra("CONTACT_ID", -1)
        if (contactId == -1) {
            Toast.makeText(this, "Грешка при зареждане на контакта", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etName = findViewById<EditText>(R.id.etUpdateName)
        val etPhone = findViewById<EditText>(R.id.etUpdatePhone)
        val etEmail = findViewById<EditText>(R.id.etUpdateEmail)
        val etAddress = findViewById<EditText>(R.id.etUpdateAddress)
        val ivPhoto = findViewById<ImageView>(R.id.ivUpdatePhoto)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnMaps = findViewById<Button>(R.id.btnMaps)


        // Зареждане на данните на контакта асинхронно
        lifecycleScope.launch {
            currentContact = vm.getContactById(contactId)
            currentContact?.let { contact ->
                etName.setText(contact.name)
                etPhone.setText(contact.phone)
                etEmail.setText(contact.email)
                etAddress.setText(contact.address)

                val source = when {
                    !contact.photoPath.isNullOrEmpty() -> File(contact.photoPath)
                    !contact.photoUrl.isNullOrEmpty() -> contact.photoUrl
                    else -> android.R.drawable.sym_def_app_icon
                }
                ivPhoto.load(source) { crossfade(true) }
            }
        }

        btnUpdate.setOnClickListener {
            currentContact?.let { contact ->
                val updatedContact = contact.copy(
                    name = etName.text.toString(),
                    phone = etPhone.text.toString(),
                    email = etEmail.text.toString(),
                    address = etAddress.text.toString()
                )
                vm.updateContact(updatedContact)
                Toast.makeText(this, "Контактът е обновен", Toast.LENGTH_SHORT).show()
                finish() // Затваряме екрана и се връщаме в MainActivity
            }
        }

        btnDelete.setOnClickListener {
            currentContact?.let { contact ->
                vm.deleteContact(contact)
                Toast.makeText(this, "Контактът е изтрит", Toast.LENGTH_SHORT).show()
                finish()
            }
        }


        btnMaps.setOnClickListener {
            currentContact?.let{
                val bundle = Bundle()
                bundle.putString("CONTACT_NAME", it.name)
                bundle.putString("CONTACT_ADDRESS", it.address)

                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent, bundle)
            }
        }
    }
}
