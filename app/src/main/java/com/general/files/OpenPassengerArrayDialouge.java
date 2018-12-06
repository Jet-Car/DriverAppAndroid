package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.jet.driver.ChatActivity;
import com.jet.driver.R;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

public class OpenPassengerArrayDialouge {
    Context mContext;
    JSONArray data_trip;
    GeneralFunctions generalFunc;
    int i = 0;
    android.support.v7.app.AlertDialog alertDialog;

    ProgressBar LoadingProgressBar;
    boolean isnotification;

    public OpenPassengerArrayDialouge(Context mContext, JSONArray data_trip, GeneralFunctions generalFunc, boolean isnotification) {
        this.mContext = mContext;
        this.data_trip = data_trip;
        this.generalFunc = generalFunc;
        this.isnotification = isnotification;
        i = 0;
        try {
            show();
        } catch (Exception ex) {
            System.out.print("jk");
        }

    }

    public void show() throws JSONException {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle("");
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_passenger_array_detail, null);
        builder.setView(dialogView);
        LoadingProgressBar = ((ProgressBar) dialogView.findViewById(R.id.LoadingProgressBar));
        ((MTextView) dialogView.findViewById(R.id.rateTxt)).setText(generalFunc.convertNumberWithRTL((String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("vAvgRating")));
        ((MTextView) dialogView.findViewById(R.id.nameTxt)).setText((String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("vName"));
        String msg = "";
        msg = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_DETAIL");
        ((MTextView) dialogView.findViewById(R.id.passengerDTxt)).setText(msg);
        ((MTextView) dialogView.findViewById(R.id.callTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) dialogView.findViewById(R.id.msgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((SimpleRatingBar) dialogView.findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0, (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("vAvgRating")));
        String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Passenger/" + (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("iUserId") + "/"
                + (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("vImgName");
        Picasso.with(mContext)
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(((SelectableRoundedImageView) dialogView.findViewById(R.id.passengerImgView)));

        if (data_trip.length() > 1) {
            (dialogView.findViewById(R.id.iv_next)).setVisibility(View.VISIBLE);
            (dialogView.findViewById(R.id.iv_previous)).setVisibility(View.GONE);
        } else {
            (dialogView.findViewById(R.id.iv_next)).setVisibility(View.GONE);
            (dialogView.findViewById(R.id.iv_previous)).setVisibility(View.GONE);
        }
        if (i > 0) {
            (dialogView.findViewById(R.id.iv_previous)).setVisibility(View.VISIBLE);

        } else {
            (dialogView.findViewById(R.id.iv_previous)).setVisibility(View.GONE);
        }
        if (i == data_trip.length() - 1) {
            (dialogView.findViewById(R.id.iv_next)).setVisibility(View.GONE);

        } else {
            (dialogView.findViewById(R.id.iv_next)).setVisibility(View.VISIBLE);
        }
        (dialogView.findViewById(R.id.callArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                try {
                    getMaskNumber();
                } catch (Exception ex) {

                }

            }
        });


        (dialogView.findViewById(R.id.iv_previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i > 0) {
                    i--;
                    try {
                        show();
                    } catch (Exception ex) {

                    }
                    if (i == 0) {
                        (dialogView.findViewById(R.id.iv_previous)).setVisibility(View.GONE);
                    } else {
                        (dialogView.findViewById(R.id.iv_previous)).setVisibility(View.VISIBLE);
                    }

                }
            }
        });

        (dialogView.findViewById(R.id.iv_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i < data_trip.length() - 1) {
                    i++;
                    try {

                        show();
                    } catch (Exception ex) {

                    }
                    (dialogView.findViewById(R.id.iv_previous)).setVisibility(View.VISIBLE);
                    if (i == data_trip.length() - 1) {
                        (dialogView.findViewById(R.id.iv_next)).setVisibility(View.GONE);
                    } else {
                        (dialogView.findViewById(R.id.iv_next)).setVisibility(View.VISIBLE);
                    }

                }
            }
        });

        (dialogView.findViewById(R.id.msgArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                Bundle bnChat = new Bundle();
                try {
                    bnChat.putString("iFromMemberId", (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("iUserId"));
                    bnChat.putString("FromMemberImageName", (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("vImgName"));
                    bnChat.putString("iTripId", (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("iTripId"));
                    bnChat.putString("FromMemberName", (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("vName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new StartActProcess(mContext).startActWithData(ChatActivity.class, bnChat);
            }
        });

        (dialogView.findViewById(R.id.closeImg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });


        (dialogView.findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getData(i);

            }
        });
        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.show();
        if (isnotification) {
            isnotification = false;
            dialogView.findViewById(R.id.msgArea).performClick();
        }
    }

    public void getMaskNumber() throws JSONException {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", (String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                Utils.printLog("Response", "::" + responseString);
                if (responseString != null && !responseString.equals("")) {
                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);
                    if (isDataAvail == true) {
                        String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);
                        call(message);
                    } else {
                        try {
                            call((String) data_trip.getJSONObject(i).getJSONObject("PassengerDetails").get("vPhone"));
                        } catch (Exception ex) {

                        }

                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void call(String phoneNumber) {

        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            // callIntent.setData(Uri.parse("tel:" + data_trip.get("PPhoneC") + "" + data_trip.get("PPhone")));
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            mContext.startActivity(callIntent);

        } catch (Exception e) {
        }
    }

    public void getData(int t) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", CommonUtilities.app_type);
        parameters.put("AppVersion", Utils.getAppVersion());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(final String responseString) {


                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(CommonUtilities.action_str, responseString);

                    String message = generalFunc.getJsonValue(CommonUtilities.message_str, responseString);

                    if (message.equals("SESSION_OUT")) {
                        generalFunc.notifySessionTimeOut();
                        Utils.runGC();
                        return;
                    }

                    if (isDataAvail == true) {
                        generalFunc.storedata(CommonUtilities.USER_PROFILE_JSON, generalFunc.getJsonValue(CommonUtilities.message_str, responseString));
                        new OpenMainProfile(mContext, String.valueOf(i),
                                generalFunc.getJsonValue(CommonUtilities.message_str, responseString), true, generalFunc).startProcess();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ActivityCompat.finishAffinity((Activity) mContext);
                                    Utils.runGC();
                                } catch (Exception e) {

                                }
                            }
                        }, 300);


                    } else {
                        if (!generalFunc.getJsonValue("isAppUpdate", responseString).trim().equals("")
                                && generalFunc.getJsonValue("isAppUpdate", responseString).equals("true")) {

                        } else {

                            if (generalFunc.getJsonValue(CommonUtilities.message_str, responseString).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_COMPANY") ||
                                    generalFunc.getJsonValue(CommonUtilities.message_str, responseString).equalsIgnoreCase("LBL_ACC_DELETE_TXT") ||
                                    generalFunc.getJsonValue(CommonUtilities.message_str, responseString).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_DRIVER")) {

                                GenerateAlertBox alertBox = generalFunc.notifyRestartApp("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                                alertBox.setCancelable(false);
                                alertBox.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                                    @Override
                                    public void handleBtnClick(int btn_id) {

                                        if (btn_id == 1) {
//                                            generalFunc.logoutFromDevice(mContext,generalFunc,"getUserData");
                                            generalFunc.logOutUser();
                                            generalFunc.restartApp();
                                        }
                                    }
                                });
                                return;
                            }

                        }
                    }
                }
            }
        });
        exeWebServer.execute();
    }
}
