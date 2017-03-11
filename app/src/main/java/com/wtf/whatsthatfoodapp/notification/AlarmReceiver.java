package com.wtf.whatsthatfoodapp.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.EditMemoryActivity;
import com.wtf.whatsthatfoodapp.memory.Memory;


public class AlarmReceiver extends BroadcastReceiver {
    private Memory memory;
    public static final int NOTIFICATION_ID = 176;
    public static final int REQUEST_CODE = 262;
    public static final String NOTIFICATION = "ALARM";

    @Override
    public void onReceive(Context context, Intent intent){
        // Build Notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_alarm_add_white_24dp)
                        .setContentTitle("Memory requires attention")
                        .setContentText("Fill out some details...")
                        .setAutoCancel(true);

        // Action when the notification is clicked
        memory = intent.getBundleExtra("bundle").getParcelable(EditMemoryActivity.MEMORY_KEY);

        Intent resultIntent = new Intent(context,EditMemoryActivity.class);
        resultIntent.putExtra(EditMemoryActivity.MEMORY_KEY,memory);
        resultIntent.putExtra(NOTIFICATION,true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(EditMemoryActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(REQUEST_CODE,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID,mBuilder.build());

    }
}
