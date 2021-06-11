package com.example.projemanaj.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.projemanaj.R
import com.example.projemanaj.activities.MainActivity
import com.example.projemanaj.activities.SignInActivity
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    // when we receive message from firebase for notification we retrieve the data from it and show the notification to the user
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG,"FROM : ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG,"Message Data Payload : ${remoteMessage.data}")
        }
        remoteMessage.notification?.let {
            Log.e(TAG,"Message Notification Body : ${it.body}")
        }
            //retrieve the data from data
        val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
        val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!

        sendNotification(title,message)
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG,"Refreshed token : $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token : String?){
        //Implement
    }

    private fun sendNotification(title : String ,message : String){
        //if user logged in sent it to the main activity other wise sent it to the sign in activity
        val intent = if(FireStoreClass().getCurrentUserId().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        }else{
            Intent(this,SignInActivity::class.java)
        }
        //so that the activity doesn't overlap with each other one main activity open at one time
        //ONLY ONE instance at one time not multiple instances
        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TASK )
        val pendingIntent = PendingIntent.getActivity(this,
        0,intent,PendingIntent.FLAG_ONE_SHOT)
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this,
        channelId).setSmallIcon(R.drawable.ic_notification_icon_24)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,"Channel Projemanaj title",NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }
    companion object{
       private const val TAG = "MyFirebaseMsgService"
    }
}