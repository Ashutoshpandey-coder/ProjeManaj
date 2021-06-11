package com.example.projemanaj.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap


class Constants {
    companion object{
        const val USERS : String = "Users"

        const val IMAGE : String = "image"
        const val NAME : String = "name"
        const val MOBILE : String = "mobile"

        const val BOARD : String = "Board"
        const val ASSIGNED_TO : String = "assignedTo"

        const val DOCUMENT_ID : String = "documentId"
        const val TASK_LIST :String = "taskList"
        const val BOARD_DETAIL : String = "board_detail"
        const val ID : String = "id"
        const val EMAIL : String = "email"

        const val TASK_LIST_ITEM_POSITION = "task_list_item_position"
        const val CARD_LIST_ITEM_POSITION = "card_list_item_position"

        const val BOARD_MEMBERS_LIST : String = "board_members_list"
        const val SELECT : String = "Select"
        const val UN_SELECT : String = "UnSelect"

        const val PROJEMANAJ_PREFERENCES = "projeManag_pref"
        const val FCM_TOKEN_UPDATED = "fcm_token_updated"
        const val FCM_TOKEN = "fcmToken"

        const val FCM_BASE_URL : String = "https://fcm.googleapis.com/fcm/send"
        const val FCM_AUTHORIZATION : String = "authorization"
        const val FCM_KEY : String = "key"
        const val FCM_SERVER_KEY : String = "AAAA_gmRvew:APA91bHi0bm8GZWK8u1MztoihX7nP1APCqke45hhBk-1YSZ1GTSIvyLhDjWoEIzaEcsQrO72KckmdapliaKsn1dCAF0kO29yzr2cJD6RCx5aOCgD64xVtHM-i_DYnlvGLcg8EQYsZO6_"
        const val FCM_KEY_TITLE : String = "title"
        const val FCM_KEY_MESSAGE :String = "message"
        const val FCM_KEY_DATA : String = "data"
        const val FCM_KEY_TO : String = "to"

        const val READ_STORAGE_PERMISSION_CODE = 1
        const val PICK_IMAGE_REQUEST_CODE = 2

         fun showImageChooser(activity : Activity) {
            val imageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
             activity.startActivityForResult(imageIntent, PICK_IMAGE_REQUEST_CODE)
        }
         fun getFileExtension(uri: Uri?,activity: Activity): String? {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(activity.contentResolver.getType(uri!!))
        }
    }
}