package com.example.contacts
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val vm: ContactViewModel by viewModels()
    private lateinit var photoRepo: PhotoRepository
    private var pickedPhotoPath: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                pickedPhotoPath = photoRepo.saveFromUri(it)
                findViewById<ImageView>(R.id.ivPreview).setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoRepo = PhotoRepository(this)
        val adapter = ContactAdapter( {contact ->
            var i = android.content.Intent(applicationContext, UpdateDeleteActivity::class.java )
            var b : Bundle = Bundle()

            b.putInt("CONTACT_ID", contact.id)
            i.putExtras(b)
           startActivity(i, b)


        })
        findViewById<RecyclerView>(R.id.rvContacts).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
        }

        vm.allContacts.observe(this) { adapter.submitList(it) }

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnAdd = findViewById<Button>(R.id.btnAdd)

        etEmail.addTextChangedListener { validate(etEmail, etPhone, btnAdd) }
        etPhone.addTextChangedListener { validate(etEmail, etPhone, btnAdd) }

        findViewById<Button>(R.id.btnPick).setOnClickListener { pickImage.launch("image/*") }

        btnAdd.setOnClickListener {
            val contact = Contact(
                name = etName.text.toString(),
                phone = etPhone.text.toString(),
                email = etEmail.text.toString(),
                address = findViewById<EditText>(R.id.etAddress).text.toString(),
                photoUrl = findViewById<EditText>(R.id.etPhotoUrl).text.toString(),
                photoPath = pickedPhotoPath ?: ""
            )
            vm.addContact(contact)

            etName.text.clear()
            etPhone.text.clear()
            etEmail.text.clear()
            pickedPhotoPath = null
            findViewById<ImageView>(R.id.ivPreview).setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun validate(email: EditText, phone: EditText, btn: Button) {
        val isEmailValid = email.text.contains("@")
        val isPhoneValid = phone.text.length >= 7
        email.setTextColor(if (isEmailValid || email.text.isEmpty()) Color.BLACK else Color.RED)
        phone.setTextColor(if (isPhoneValid || phone.text.isEmpty()) Color.BLACK else Color.RED)
        btn.isEnabled = isEmailValid && isPhoneValid && email.text.isNotEmpty()
    }
}