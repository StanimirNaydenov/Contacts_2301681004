package com.example.contacts
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import coil.load
import java.io.File
import androidx.recyclerview.widget.ListAdapter

class ContactAdapter( protected val onItemClick: (Contact) -> Unit ) : ListAdapter<Contact, ContactAdapter.VH>(DIFF) {





    object DIFF : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(old: Contact, new: Contact) = old.id == new.id
        override fun areContentsTheSame(old: Contact, new: Contact) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))

        var contact = getItem(position)

        holder.itemView.setOnClickListener {
            onItemClick(contact)
        }

    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tvName)
        private val phone: TextView = view.findViewById(R.id.tvPhone)
        private val img: ImageView = view.findViewById(R.id.img)

        fun bind(c: Contact) {
            name.text = c.name
            phone.text = c.phone

            val source = when {
                !c.photoPath.isNullOrEmpty() -> File(c.photoPath)
                !c.photoUrl.isNullOrEmpty() -> c.photoUrl
                else -> android.R.drawable.sym_def_app_icon
            }
            img.load(source) { crossfade(true); placeholder(android.R.drawable.sym_def_app_icon) }
        }
    }
}


