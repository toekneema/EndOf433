package com.example.endof433;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        String details;
        Bundle extras = intent.getExtras();
        if(extras == null){
            details = "";
        } else{
            details = extras.getString("details");
        }

        NotificationCompat.Builder nb = notificationHelper.getChannel1Notification("Table tennis practice reminder!",
                details);
        notificationHelper.getManager().notify(1, nb.build());

    }
}
