package com.ebabu.tikautsav.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import com.ebabu.tikautsav.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Utility {

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    fun nameValidate(
        mContext: Context,
        inputLayout: TextInputLayout,
        editText: TextInputEditText
    ): Boolean {
        val name = editText.text.toString().trim { it <= ' ' }
        inputLayout.error = null
        inputLayout.errorIconDrawable = null
        if (name.isEmpty()) {
            inputLayout.error = mContext.getString(R.string.name_error_msg)
            editText.requestFocus()
            return false
        }
        if (name.length < 3) {
            inputLayout.error = mContext.getString(R.string.name_short)
            editText.requestFocus()
            return false
        }
        return true
    }

    fun cityValidate(
        mContext: Context,
        inputLayout: TextInputLayout,
        editText: TextInputEditText
    ): Boolean {
        val name = editText.text.toString().trim { it <= ' ' }
        inputLayout.error = null
        inputLayout.errorIconDrawable = null
        if (name.isEmpty()) {
            inputLayout.error = mContext.getString(R.string.name_error_msg)
            editText.requestFocus()
            return false
        }
        return true
    }

    fun shareIntent(context: Context, text: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        i.putExtra(Intent.EXTRA_TEXT, "$text https://play.google.com/store/apps/details?id=${context.packageName}".trim())
        context.startActivity(Intent.createChooser(i, "Share via:"))
    }

}