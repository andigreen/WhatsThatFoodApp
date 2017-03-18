package com.wtf.whatsthatfoodapp.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.EditMemoryActivity;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;


public class AlarmReceiver extends BroadcastReceiver {
    private Memory memory;

    @Override
    public void onReceive(Context context, Intent intent){
        // Build Notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_alarm_add_white_24dp)
                        .setContentTitle("Memory requires attention")
                        .setContentText("Fill out some details...")
                        .setAutoCancel(true);

        // Update the memory
        FirebaseApp.initializeApp(context);
        memory = intent.getBundleExtra("bundle").getParcelable(EditMemoryActivity.MEMORY_KEY);

        // Action when the notification is clicked
        Intent resultIntent = new Intent(context,EditMemoryActivity.class);
        resultIntent.putExtra(EditMemoryActivity.MEMORY_KEY,memory);
        resultIntent.putExtra(EditMemoryActivity.NOTIFICATION,true);
        int uniqueID = (int) System.currentTimeMillis();
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, uniqueID, resultIntent, 0);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = (int) System.currentTimeMillis();
        mNotificationManager.notify(notificationID,mBuilder.build());

    }
}
