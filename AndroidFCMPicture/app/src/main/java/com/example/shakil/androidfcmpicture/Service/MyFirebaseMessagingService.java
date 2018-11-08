package com.example.shakil.androidfcmpicture.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.example.shakil.androidfcmpicture.Config.Config;
import com.example.shakil.androidfcmpicture.Display;
import com.example.shakil.androidfcmpicture.Helper.NotificationHelper;
import com.example.shakil.androidfcmpicture.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotificationWithImageLevel26(bitmap);
            } else {
                showNotificationWithImage(bitmap);
            }

        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationWithImageLevel26(Bitmap bitmap) {
        NotificationHelper helper = new NotificationHelper(getBaseContext());

        Notification.Builder builder = helper.getChannel(Config.title, Config.message, bitmap);
        helper.getManager().notify(0, builder.build());
    }

    private void showNotificationWithImage(Bitmap bitmap) {
        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
        style.setSummaryText(Config.message);
        style.bigPicture(bitmap);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(getApplicationContext(), Display.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(Config.title)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)
                .setStyle(style);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null) {
            getImage(remoteMessage);
        }
    }

    private void getImage(final RemoteMessage remoteMessage) {
        //Set message and title
        Config.message = remoteMessage.getNotification().getBody();
        Config.title = remoteMessage.getNotification().getTitle();

        //Create thread to fetch image from Notification
        if (remoteMessage.getData() != null) {
            Config.imageLink = remoteMessage.getData().get("image");
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    //Get image from data notification
                    Picasso.get().load(remoteMessage.getData().get("image")).into(target);
                }
            });
        }
    }
}
