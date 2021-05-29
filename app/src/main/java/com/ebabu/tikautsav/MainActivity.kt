package com.ebabu.tikautsav

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.ebabu.tikautsav.databinding.ActivityMainBinding
import com.ebabu.tikautsav.utils.Glide4Engine
import com.ebabu.tikautsav.utils.PermissionsUtils
import com.ebabu.tikautsav.utils.Utility
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_CHOOSE = 1001
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()
    }

    private fun init() {
        val db = Firebase.firestore

        val totalVaccines = db.collection("totalVaccines").document("MP")
        val vaccinated = db.collection("vaccinated").document("MP")

        totalVaccines.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                binding.textView2.text = "${snapshot.data?.get("population")}"
            }
        }

        vaccinated.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                binding.textView4.text = "${snapshot.data?.get("vaccinated")}"
            }
        }

        binding.imageView2.setOnClickListener {
            Utility.shareIntent(this, getString(R.string.app_share))
        }
        binding.materialButton.setOnClickListener {
            if (!PermissionsUtils.requiredPermissionsGranted(this)) return@setOnClickListener
            else if (!Utility.nameValidate(
                    this,
                    binding.textInputLayout,
                    binding.etName
                )
            ) return@setOnClickListener
            else if (!Utility.cityValidate(
                    this,
                    binding.textInputLayout2,
                    binding.etCity
                )
            ) return@setOnClickListener
            showPicker()
        }
    }

    private fun showPicker() {
        Matisse.from(this)
            .choose(MimeType.ofImage(), false)
            .theme(R.style.Matisse_Zhihu)
            .capture(true)
            .captureStrategy(CaptureStrategy(true, "$packageName.provider"))
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .thumbnailScale(0.85f)
            .imageEngine(Glide4Engine())
            .originalEnable(false)
            .showSingleMediaType(true)
            .forResult(REQUEST_CODE_CHOOSE)
    }

    private fun startCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .setCropMenuCropButtonTitle(getString(R.string.crop))
            .start(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionsUtils.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            if (PermissionsUtils.areAllPermissionsGranted(grantResults)) {
                showPicker()
            } else {
                Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                val intent = Intent(this, SavePhotoActivity::class.java)
                intent.putExtra("name", binding.etName.text.toString().trim())
                intent.putExtra("city", binding.etCity.text.toString().trim())
                intent.putExtra("uri", result.uri.toString())
                startActivity(intent)
            } else if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
                data?.let {
                    val path = Matisse.obtainPathResult(data)[0]
                    val file = File(path)
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "$packageName.provider",
                        file
                    )
                    startCrop(photoURI)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}