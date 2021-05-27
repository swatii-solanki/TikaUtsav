package com.ebabu.tikautsav.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.ebabu.tikautsav.R
import java.util.*

object PermissionsUtils {

    internal const val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 2

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    fun requiredPermissionsGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsNeeded: MutableList<String> = ArrayList()
            val permissionsList: MutableList<String> = ArrayList()
            if (!addPermission(
                    context,
                    permissionsList,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                permissionsNeeded.add(context.getString(R.string.txt_write_external_storage))
            }
            if (!addPermission(
                    context,
                    permissionsList,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                permissionsNeeded.add(context.getString(R.string.txt_read_external_storage))
            }
            if (!addPermission(context, permissionsList, Manifest.permission.CAMERA)) {
                permissionsNeeded.add(context.getString(R.string.txt_camera))
            }

            if (permissionsList.size > 0) {
                (context as Activity).requestPermissions(
                    permissionsList.toTypedArray(),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
                )
                return false
            }
        }
        return true
    }

    // Add permission
    private fun addPermission(
        context: Context,
        permissionsList: MutableList<String>,
        permission: String
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission)
                if (!(context as Activity).shouldShowRequestPermissionRationale(permission)) return false
            }
        }
        return true
    }

    // For check permissions
    fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

}