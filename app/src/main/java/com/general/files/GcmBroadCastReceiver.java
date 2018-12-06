package com.general.files;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.jet.driver.CabRequestedActivity;
import com.jet.driver.R;
import com.utils.CommonUtilities;
import com.utils.Utils;

import java.util.List;

/**
 * Created by Admin on 12-07-2016.
 */
public class GcmBroadCastReceiver extends BroadcastReceiver {
    GeneralFunctions generalFunc;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (generalFunc == null) {
            generalFunc = new GeneralFunctions(context);
        }

        if (intent.getAction().equals(CommonUtilities.passenger_message_arrived_intent_action) && intent != null) {
            String json_message = intent.getExtras().getString(CommonUtilities.passenger_message_arrived_intent_key);
//            mainAct.onGcmMessageArrived(message);

            ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

            ComponentName componentInfo = taskInfo.get(0).topActivity;
            String packageName = componentInfo.getPackageName();

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = powerManager.isScreenOn();

            String codeKey = CommonUtilities.DRIVER_REQ_CODE_PREFIX_KEY + generalFunc.getJsonValue("MsgCode", json_message);

//            if (generalFunc.isTripStatusMsgExist(json_message)) {
//                return;
//            }


            if (generalFunc.retrieveValue(codeKey).equals("")) {

                String MessageData = generalFunc.getJsonValue("Message", json_message);

                if (MessageData.equals("CabRequested")) {


                    if (!generalFunc.getJsonValue("tSessionId", json_message).equals("")) {
                        if (!generalFunc.getJsonValue("tSessionId", json_message).equals(generalFunc.retrieveValue(Utils.SESSION_ID_KEY))) {

                            return;

                        }
                    }


                    if (packageName.equals("com.jet.driver")) {


                        Utils.printLog("isScreenOn", "isScreenOn::" + isScreenOn);
                        if (isScreenOn == false) {

                            Utils.printLog("canDrawOverlayViews", generalFunc.canDrawOverlayViews(context) + "");
                            if (generalFunc.canDrawOverlayViews(context) == true) {
                                Utils.printLog("permission", "" + generalFunc.canDrawOverlayViews(context));
                                OpenWindowDialScreen(context, json_message);
                            } else {
                                generateNotification_callingFromUser(context, json_message);
                            }


                        } else {

                            Intent show_timer = new Intent();
                            show_timer.setClass(context, CabRequestedActivity.class);
                            show_timer.putExtra("Message", json_message);

                            show_timer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(show_timer);

//                            Bundle bn = new Bundle();
//                            bn.putString("Message",json_message);
//                            Utils.printLog("Dismiss","Start");
//                            (new StartActProcess(context)).startActWithData(CabRequestedActivity.class,bn);
                        }

                    } else {
                        Utils.printLog("isScreenOn", "" + isScreenOn);
                        if (generalFunc.canDrawOverlayViews(context) == true) {
                            Utils.printLog("permission", "" + generalFunc.canDrawOverlayViews(context));
                            OpenWindowDialScreen(context, json_message);
                        } else {
                            generateNotification_callingFromUser(context, json_message);
                        }
                    }
                }

                generalFunc.storedata(codeKey, "true");

            }

        }
    }

    public void OpenWindowDialScreen(final Context context, final String message) {
//        context.startService(new Intent(context, ChatHeadService.class));
        if (generalFunc.getJsonValue("REQUEST_TYPE", message) != null) {
            if (generalFunc.getJsonValue("REQUEST_TYPE", message).equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                Utils.generateNotification(context, generalFunc.retrieveLangLBl("", "LBL_TRIP_USER_WAITING"));
            } else {
                Utils.generateNotification(context, generalFunc.retrieveLangLBl("", "LBL_DELIVERY_SENDER_WAITING"));
            }

        } else {
            Utils.generateNotification(context, generalFunc.retrieveLangLBl("", "LBL_TRIP_USER_WAITING"));

        }
        MyApp.getInstance().stopAlertService();

        Utils.printLog("Window", "OPEN");
        generalFunc.storedata(CommonUtilities.DRIVER_ACTIVE_REQ_MSG_KEY /*+ msgCode*/, message);

        Intent it = new Intent(context, ChatHeadService.class);
        it.putExtra("Message", message);
        context.startService(it);
    }

    private void generateNotification_callingFromUser(Context context, String message) {
        //  WakeLocker.acquire(context);

        int icon = R.mipmap.ic_launcher;
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, CabRequestedActivity.class);

        notificationIntent.putExtra("Message", "" + message);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.ic_stat_driver_logo);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_launcher));
            if (generalFunc.getJsonValue("REQUEST_TYPE", message) != null) {
                if (generalFunc.getJsonValue("REQUEST_TYPE", message).equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                    mBuilder.setContentTitle(title).setContentText(generalFunc.retrieveLangLBl("Passenger is waiting For you", "LBL_TRIP_USER_WAITING")).setContentIntent(intent);
                } else {
                    mBuilder.setContentTitle(title).setContentText(generalFunc.retrieveLangLBl("Passenger is waiting For you", "LBL_DELIVERY_SENDER_WAITING")).setContentIntent(intent);
                }

            } else {
                mBuilder.setContentTitle(title).setContentText(generalFunc.retrieveLangLBl("Passenger is waiting For you", "LBL_TRIP_USER_WAITING")).setContentIntent(intent);

            }

            mBuilder.setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true);
            mBuilder.setColor(context.getResources().getColor(R.color.appThemeColor_1));
        } else {
            mBuilder.setSmallIcon(R.drawable.ic_stat_driver_logo);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_launcher));
            if (generalFunc.getJsonValue("REQUEST_TYPE", message) != null) {
                if (generalFunc.getJsonValue("REQUEST_TYPE", message).equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                    mBuilder.setContentTitle(title).setContentText(generalFunc.retrieveLangLBl("Passenger is waiting For you", "LBL_TRIP_USER_WAITING")).setContentIntent(intent);
                } else {
                    mBuilder.setContentTitle(title).setContentText(generalFunc.retrieveLangLBl("Passenger is waiting For you", "LBL_DELIVERY_SENDER_WAITING")).setContentIntent(intent);
                }

            } else {
                mBuilder.setContentTitle(title).setContentText(generalFunc.retrieveLangLBl("Passenger is waiting For you", "LBL_TRIP_USER_WAITING")).setContentIntent(intent);

            }
            mBuilder.setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true);
            mBuilder.setColor(context.getResources().getColor(R.color.appThemeColor_1));
        }
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationmanager.notify(Utils.NOTIFICATION_ID, mBuilder.build());

        Utils.printLog("Data", "Store");
        String msgCode = generalFunc.getJsonValue("MsgCode", message);
        generalFunc.storedata(CommonUtilities.DRIVER_ACTIVE_REQ_MSG_KEY/* + msgCode*/, message);

    }

}
