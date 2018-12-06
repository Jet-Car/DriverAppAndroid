package com.jet.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.MTextView;

public class UploadDocTypeWiseActivity extends AppCompatActivity {


    LinearLayout uberxArea, deliverArea, rideArea;
    GeneralFunctions generalFunctions;
    MTextView ridetitleTxt, deliverytitleTxt, uberxtitleTxt;
    MTextView titleTxt;
    ImageView backImgView;

    public static int ADDVEHICLE = 1;

    int totalVehicles = 0;
    String app_type;
    String userProfileJson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_doc_type_wise);
        generalFunctions = new GeneralFunctions(getActContext());
        initView();
    }

    public void initView() {
        uberxArea = (LinearLayout) findViewById(R.id.uberxArea);
        rideArea = (LinearLayout) findViewById(R.id.rideArea);
        deliverArea = (LinearLayout) findViewById(R.id.deliverArea);
        ridetitleTxt = (MTextView) findViewById(R.id.ridetitleTxt);
        deliverytitleTxt = (MTextView) findViewById(R.id.deliverytitleTxt);
        uberxtitleTxt = (MTextView) findViewById(R.id.uberxtitleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        uberxArea.setOnClickListener(new setOnClickList());
        rideArea.setOnClickListener(new setOnClickList());
        deliverArea.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());

        userProfileJson = generalFunctions.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
        app_type = generalFunctions.getJsonValue("APP_TYPE", userProfileJson);


        totalVehicles = getIntent().getIntExtra("totalVehicles", 0);



            if (generalFunctions.getJsonValue("eShowRideVehicles", userProfileJson).equalsIgnoreCase("yes")) {
                rideArea.setVisibility(View.VISIBLE);
            } else {
                rideArea.setVisibility(View.GONE);
            }

            if (generalFunctions.getJsonValue("eShowDeliveryVehicles", userProfileJson).equalsIgnoreCase("yes")) {
                deliverArea.setVisibility(View.VISIBLE);
            } else {
                deliverArea.setVisibility(View.GONE);
            }




        if ((getIntent().getStringExtra("isChange") != null && getIntent().getStringExtra("isChange").equalsIgnoreCase("Yes"))) {
            uberxArea.setVisibility(View.GONE);

        }


        if (getIntent().getStringExtra("selView").equalsIgnoreCase("doc")) {
            ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_UPLOAD_DOC_RIDE"));
            deliverytitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_UPLOAD_DOC_DELIVERY"));
            uberxtitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_UPLOAD_DOC_UFX"));
        } else {



                ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_VEHICLES_RIDE"));
                deliverytitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_VEHICLES_DELIVERY"));
                uberxtitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_OTHER_SERVICES"));

        }
        titleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_SELECT_TYPE"));


//        new CreateRoundedView(getActContext().getResources().getColor(R.color.white), Utils.dipToPixels(getActContext(), 35), 2,
//                getActContext().getResources().getColor(R.color.white), deliverImgViewsel);


    }

    public Context getActContext() {
        return UploadDocTypeWiseActivity.this;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            Bundle bn = new Bundle();
            bn.putString("PAGE_TYPE", "Driver");
            bn.putString("iDriverVehicleId", "");
            bn.putString("doc_file", "");
            bn.putString("iDriverVehicleId", "");
            Utils.hideKeyboard(UploadDocTypeWiseActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    UploadDocTypeWiseActivity.super.onBackPressed();
                    break;
                case R.id.rideArea:
                    bn.putString("seltype", Utils.CabGeneralType_Ride);
                    if (getIntent().getStringExtra("selView").equalsIgnoreCase("doc")) {
                        new StartActProcess(getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
                    } else {
                        if (totalVehicles > 0) {
                            new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
                        } else {
                            new StartActProcess(getActContext()).startActForResult(AddVehicleActivity.class, bn, ADDVEHICLE);
                        }

                    }
                    break;
                case R.id.deliverArea:
                    bn.putString("seltype", "Delivery");
                    if (getIntent().getStringExtra("selView").equalsIgnoreCase("doc")) {
                        new StartActProcess(getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
                    } else {
                        if (totalVehicles > 0) {
                            new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
                        } else {
                            new StartActProcess(getActContext()).startActForResult(AddVehicleActivity.class, bn, ADDVEHICLE);
                        }
                    }
                    break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (data.getStringExtra("iDriverVehicleId") != null && !data.getStringExtra("iDriverVehicleId").equalsIgnoreCase
                    ("")) {
                totalVehicles = 1;




                    ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_VEHICLES_RIDE"));
                    deliverytitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_VEHICLES_DELIVERY"));
                    uberxtitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_OTHER_SERVICES"));

            }
            //handle total vehicles

        }
    }
}
