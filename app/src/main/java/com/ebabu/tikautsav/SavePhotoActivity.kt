package com.ebabu.tikautsav

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ebabu.tikautsav.databinding.ActivitySavePhotoBinding
import com.ebabu.tikautsav.utils.Utility
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.FileOutputStream


class SavePhotoActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SavePhotoActivity"
    }

    private lateinit var binding: ActivitySavePhotoBinding
    private var count  = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_save_photo)
        init()
    }

    private fun init() {
        val db = Firebase.firestore
        val uri = intent.getStringExtra("uri")
        val name = intent.getStringExtra("name")
        val city = intent.getStringExtra("city")
        uri?.let {
            binding.iv.setImageURI(Uri.parse(uri))
        }

        val user = hashMapOf(
            "name" to name,
            "city" to city,
        )

        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

        val vaccinated = db.collection("vaccinated").document("MP")
        vaccinated.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                count = snapshot.data?.get("vaccinated") as Long
                count += 1

            }
        }

        db.collection("vaccinated")
            .document("MP")
            .update("vaccinated", ++count)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e_ -> Log.w(TAG, "Error writing document", e_) }


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
        val bitmap = Bitmap.createBitmap(
            view.width, view.height,
            Bitmap.Config.ARGB_8888
        )
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