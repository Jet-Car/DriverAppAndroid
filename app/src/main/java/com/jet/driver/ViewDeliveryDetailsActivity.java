package com.jet.driver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import java.util.HashMap;

public class ViewDeliveryDetailsActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    ErrorView errorView;
    ProgressBar loading;

    View contentArea;

    String data_message;

    HashMap<String, String> trip_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_delivery_details);

        generalFunc = new GeneralFunctions(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        contentArea = findViewById(R.id.contentArea);
        errorView = (ErrorView) findViewById(R.id.errorView);
        loading = (ProgressBar) findViewById(R.id.loading);

        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        getDeliveryData();

        (findViewById(R.id.senderCallArea)).setOnClickListener(new setOnClickList());
        (findViewById(R.id.senderMsgArea)).setOnClickListener(new setOnClickList());
        (findViewById(R.id.receiverCallArea)).setOnClickListener(new setOnClickList());
        (findViewById(R.id.receiverMsgArea)).setOnClickListener(new setOnClickList());
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Delivery Details", "LBL_DELIVERY_DETAILS"));

        ((MTextView) findViewById(R.id.senderHTxt)).setText(generalFunc.retrieveLangLBl("Sender", "LBL_SENDER"));
        ((MTextView) findViewById(R.id.senderCallTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) findViewById(R.id.senderMsgTxt)).setText(generalFunc.retrieveLangLBl("Chat", "LBL_CHAT_TXT"));
//        ((MTextView) findViewById(R.id.senderMsgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((MTextView) findViewById(R.id.receiverHTxt)).setText(generalFunc.retrieveLangLBl("Recipient", "LBL_RECIPIENT"));
        ((MTextView) findViewById(R.id.receiverCallTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) findViewById(R.id.receiverMsgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((MTextView) findViewById(R.id.packageTypeHTxt)).setText(generalFunc.retrieveLangLBl("Package Type", "LBL_PACKAGE_TYPE"));
        ((MTextView) findViewById(R.id.packageDetailsHTxt)).setText(generalFunc.retrieveLangLBl("Package Details", "LBL_PACKAGE_DETAILS"));
        ((MTextView) findViewById(R.id.pickUpInsHTxt)).setText(generalFunc.retrieveLangLBl("Pickup instruction", "LBL_PICK_UP_INS"));
        ((MTextView) findViewById(R.id.deliveryInsHTxt)).setText(generalFunc.retrieveLangLBl("Delivery instruction", "LBL_DELIVERY_INS"));
    }


    public void sendMsg(String phoneNumber, boolean isdefaultMsg) {
        try {

            if (isdefaultMsg) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", "" + phoneNumber);
                startActivity(smsIntent);
            } else {

                trip_data = (HashMap<String, String>) getIntent().getSerializableExtra("data_trip");
                Bundle bnChat = new Bundle();
                bnChat.putString("iFromMemberId", trip_data.get("PassengerId"));
                bnChat.putString("FromMemberImageName", trip_data.get("PPicName"));
                bnChat.putString("iTripId", trip_data.get("iTripId"));
                bnChat.putString("FromMemberName", trip_data.get("PName"));

                new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void call(String phoneNumber) {
        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    public void getMaskNumber() {

        trip_data = (HashMap<String, String>) getIntent().getSerializableExtra("data_trip");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", trip_data.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

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
                        call(generalFunc.getJsonValue("senderMobile", data_message));

                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void getDeliveryData() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (contentArea.getVisibility() == View.VISIBLE) {
            contentArea.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadDeliveryDetails");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iTripId", getIntent().getStringExtra("TripId"));
        parameters.put("appType", CommonUtilities.app_type);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    closeLoader();

                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {

                        setData(generalFunc.getJsonValue(CommonUtilities.message_str, responseString));
                    } else {
                        generateErrorView();
                    }
                } else {
                    generateErrorView();
                }
            }
        });
        exeWebServer.execute();
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void setData(String message) {

        this.data_message = message;

        ((MTextView) findViewById(R.id.senderNameTxt)).setText(generalFunc.getJsonValue("senderName", message));
        ((MTextView) findViewById(R.id.senderMobileTxt)).setText(generalFunc.getJsonValue("senderMobile", message));
        ((MTextView) findViewById(R.id.receiverNameTxt)).setText(generalFunc.getJsonValue("vReceiverName", message));
        ((MTextView) findViewById(R.id.receiverMobileTxt)).setText(generalFunc.getJsonValue("vReceiverMobile", message));
        ((MTextView) findViewById(R.id.packageTypeVTxt)).setText(generalFunc.getJsonValue("packageType", message));
        ((MTextView) findViewById(R.id.packageDetailsVTxt)).setText(generalFunc.getJsonValue("tPackageDetails", message));
        ((MTextView) findViewById(R.id.pickUpInsVTxt)).setText(generalFunc.getJsonValue("tPickUpIns", message));
        ((MTextView) findViewById(R.id.deliveryInsVTxt)).setText(generalFunc.getJsonValue("tDeliveryIns", message));

        contentArea.setVisibility(View.VISIBLE);
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getDeliveryData();
            }
        });
    }

    public Context getActContext() {
        return ViewDeliveryDetailsActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(ViewDeliveryDetailsActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    ViewDeliveryDetailsActivity.super.onBackPressed();
                    break;
                case R.id.senderCallArea:
                    // call(generalFunc.getJsonValue("senderMobile", data_message));
                    getMaskNumber();
                    break;
                case R.id.senderMsgArea:
                    sendMsg(generalFunc.getJsonValue("senderMobile", data_message), false);
                    break;
                case R.id.receiverCallArea:
                    call(generalFunc.getJsonValue("vReceiverMobile", data_message));
                    break;
                case R.id.receiverMsgArea:
                    sendMsg(generalFunc.getJsonValue("vReceiverMobile", data_message), true);
                    break;

            }
        }
    }
}
