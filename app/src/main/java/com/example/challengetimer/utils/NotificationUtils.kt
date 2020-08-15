package com.example.challengetimer.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.challengetimer.MainActivity
import com.example.challengetimer.R
import com.example.challengetimer.database.Challenge

private val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(challenge: Challenge?, messageBody: String, applicationContext: Context) {

    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val  bruhImage = BitmapFactory.decodeResource(applicationContext.resources, when (challenge?.color) {
        applicationContext.getColor(R.color.pinkColor) -> R.drawable.rank_zero_pink_image
        applicationContext.getColor(R.color.violetColor) -> R.drawable.rank_zero_violet_image
        applicationContext.getColor(R.color.blueColor) -> R.drawable.rank_zero_blue_image
        else -> R.drawable.bruh_image
    })
    val bigTxtStyle = NotificationCompat.BigTextStyle()

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.timer_notification_channel_id))
        .setSmallIcon(R.drawable.bruh_image)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setLargeIcon(bruhImage)
        .setStyle(bigTxtStyle)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(NOTIFICATION_ID, builder.build())
}