package com.example.contacts

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class QRCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        val vcfData = intent.getStringExtra("VCF_DATA")
        val contactName = intent.getStringExtra("CONTACT_NAME")

        findViewById<TextView>(R.id.tvQrContactName).text = contactName ?: "Контакт"

        if (!vcfData.isNullOrEmpty()) {
            val bitmap = generateQRCode(vcfData)
            if (bitmap != null) {
                findViewById<ImageView>(R.id.ivQrCode).setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "Грешка при генериране на QR код", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Липсват данни за контакта", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateQRCode(text: String): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            // Генерираме матрица 512x512 пиксела
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    // Задаваме черен цвят за пикселите от кода и бял за фона
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}