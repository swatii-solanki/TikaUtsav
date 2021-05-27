package com.ebabu.tikautsav

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ebabu.tikautsav.databinding.ActivitySavePhotoBinding
import com.ebabu.tikautsav.utils.Utility
import java.io.FileOutputStream


class SavePhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavePhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_save_photo)
        init()
    }

    private fun init() {
        val uri = intent.getStringExtra("uri")
        uri?.let {
            binding.iv.setImageURI(Uri.parse(uri))
        }
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.materialButton.setOnClickListener {
            val bm = getBitmapFromView(binding.materialCardView)
            val photoUri = bm?.let { getImageUri(it) }
            val intent = Intent(Intent.ACTION_SEND)
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, "The text you wanted to share")
            intent.putExtra(Intent.EXTRA_STREAM, photoUri)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.whtsapp_error), Toast.LENGTH_SHORT).show()
            }
        }
        binding.materialButton2.setOnClickListener {
            val bm = getBitmapFromView(binding.materialCardView)
            saveBitmapIntoDevice(bm)
        }
    }


    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(canvas)
        return bitmap
    }

    private fun getImageUri(photo: Bitmap): Uri? {
        val file = Utility.createImageFile(this)
        val stream = FileOutputStream(file)
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
        return Uri.parse(file.absolutePath)
    }

    private fun saveBitmapIntoDevice(photo: Bitmap?) {
        MediaStore.Images.Media.insertImage(
                contentResolver,
                photo,
                "CovidTikaImage",
                null
        )
        Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
    }
}