package com.ebabu.tikautsav

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.ebabu.tikautsav.databinding.ActivityMainBinding
import com.ebabu.tikautsav.utils.Glide4Engine
import com.ebabu.tikautsav.utils.PermissionsUtils
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
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()
    }

    private fun init() {
        binding.materialButton.setOnClickListener {
            if (!PermissionsUtils.requiredPermissionsGranted(this)) {
                return@setOnClickListener
            }
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

//    private fun openCamera() {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            // Ensure that there's a camera activity to handle the intent
//            takePictureIntent.resolveActivity(packageManager)?.also {
//                // Create the File where the photo should go
//                photoFile = try {
//                    Utility.createImageFile(this)
//                } catch (ex: IOException) {
//                    // Error occurred while creating the File
//                    ex.printStackTrace()
//                    null
//                }
//                // Continue only if the File was successfully created
//                photoFile?.also {
//                    val photoURI: Uri = FileProvider.getUriForFile(
//                            this,
//                            "com.example.android.fileprovider",
//                            it
//                    )
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    launchCameraIntent.launch(takePictureIntent)
//                }
//            }
//        }
//    }
//
//    private val launchCameraIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        try {
//            if (result.resultCode == RESULT_OK) {
//                Log.d(TAG, "onActivityResult: ${photoFile?.absolutePath}")
//                val photoURI: Uri = FileProvider.getUriForFile(
//                        this,
//                        "com.example.android.fileprovider",
//                        photoFile!!
//                )
//                startCrop(photoURI)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

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