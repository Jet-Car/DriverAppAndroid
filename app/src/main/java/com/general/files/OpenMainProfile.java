package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.gson.JsonArray;
import com.jet.driver.AccountverificationActivity;
import com.jet.driver.ActiveTripActivity;
import com.jet.driver.CollectPaymentActivity;
import com.jet.driver.DriverArrivedActivity;
import com.jet.driver.MainActivity;
import com.jet.driver.SuspendedDriver_Activity;
import com.jet.driver.TripRatingActivity;
import com.utils.AnimateMarker;
import com.utils.CommonUtilities;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 29-06-2016.
 */
public class OpenMainProfile implements GetLocationUpdates.LocationUpdates {
    private final JSONObject userProfileJsonObj;
    Context mContext;
    String responseString;
    boolean isCloseOnError;
    GeneralFunctions generalFun;
    boolean isnotification = false;
    AnimateMarker animateMarker;
    JSONArray last_trip_data;
    JSONArray passenger_data;
    Location location;
    HashMap<String, String> map;
    ArrayList<Location> loc = new ArrayList<>();
    ArrayList<String> process = new ArrayList<>();
    double check_distance;
    int positionSmallestDist;
    Bundle bn;
    String vTripStatus;
    boolean lastTripExist;
    public static int isPool;
    GPSTracker tracker;
    String position;
    int t = 0;
    int j = 0;

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;

        this.responseString = generalFun.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
        this.check_distance=0;
        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
        animateMarker = new AnimateMarker();
        tracker = new GPSTracker(mContext);

    }

    public OpenMainProfile(Context mContext, String position, String responseString, boolean isCloseOnError, GeneralFunctions generalFun) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
        this.position = position;
        this.check_distance=0;
        this.responseString = generalFun.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
        animateMarker = new AnimateMarker();
        tracker = new GPSTracker(mContext);
    }

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun, boolean isnotification) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
        this.isnotification = isnotification;

        this.responseString = generalFun.retrieveValue(CommonUtilities.USER_PROFILE_JSON);

        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
        animateMarker = new AnimateMarker();
        tracker = new GPSTracker(mContext);
        generalFun.storedata(CommonUtilities.DefaultCountry, generalFun.getJsonValueStr("vDefaultCountry", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.DefaultCountryCode, generalFun.getJsonValueStr("vDefaultCountryCode", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.DefaultPhoneCode, generalFun.getJsonValueStr("vDefaultPhoneCode", userProfileJsonObj));

    }

    public void startProcess() {
        generalFun.sendHeartBeat();

        // responseString = generalFun.retrieveValue(CommonUtilities.USER_PROFILE_JSON);
        setGeneralData();


        animateMarker.driverMarkerAnimFinished = true;

        bn = new Bundle();


        isPool = Integer.parseInt(generalFun.getJsonValueStr("isPool", userProfileJsonObj));

        if (isPool == 0) {
            String vTripStatus = generalFun.getJsonValueStr("vTripStatus", userProfileJsonObj);

            boolean lastTripExist = false;

            if (vTripStatus.contains("Not Active")) {


                String ratings_From_Driver_str = generalFun.getJsonValueStr("Ratings_From_Driver", userProfileJsonObj);

                if (!ratings_From_Driver_str.equals("Done")) {
                    lastTripExist = true;
                }
            }
            if (generalFun.getJsonValue("vPhone", userProfileJsonObj).equals("") || generalFun.getJsonValue("vEmail", userProfileJsonObj).equals("")) {
                if (generalFun.getMemberId() != null && !generalFun.getMemberId().equals("")) {
                    new StartActProcess(mContext).startActWithData(AccountverificationActivity.class, bn);
                }
            } else if (vTripStatus != null && !vTripStatus.equals("NONE") && !vTripStatus.equals("Cancelled")
                    && (vTripStatus.trim().equals("Active") || vTripStatus.contains("On Going Trip")
                    || vTripStatus.contains("Arrived") || lastTripExist == true)) {

                // String last_trip_data = generalFun.getJsonValue("TripDetails", userProfileJsonObj);
                JSONObject last_trip_data = generalFun.getJsonObject("TripDetails", userProfileJsonObj);
                // String passenger_data = generalFun.getJsonValue("PassengerDetails", userProfileJsonObj);
                JSONObject passenger_data = generalFun.getJsonObject("PassengerDetails", userProfileJsonObj);
                HashMap<String, String> map = new HashMap<>();

                map.put("TotalSeconds", generalFun.getJsonValueStr("TotalSeconds", userProfileJsonObj));
                map.put("TimeState", generalFun.getJsonValueStr("TimeState", userProfileJsonObj));
                map.put("iTripTimeId", generalFun.getJsonValueStr("iTripTimeId", userProfileJsonObj));

                map.put("Message", "CabRequested");
                map.put("sourceLatitude", generalFun.getJsonValueStr("tStartLat", last_trip_data));
                map.put("sourceLongitude", generalFun.getJsonValueStr("tStartLong", last_trip_data));

                map.put("tSaddress", generalFun.getJsonValueStr("tSaddress", last_trip_data));
                map.put("drivervName", generalFun.getJsonValue("vName", responseString));
                map.put("drivervLastName", generalFun.getJsonValue("vLastName", responseString));

                map.put("PassengerId", generalFun.getJsonValueStr("iUserId", last_trip_data));
                map.put("PName", generalFun.getJsonValueStr("vName", passenger_data));
                map.put("PPicName", generalFun.getJsonValueStr("vImgName", passenger_data));
                map.put("PFId", generalFun.getJsonValueStr("vFbId", passenger_data));
                map.put("PRating", generalFun.getJsonValueStr("vAvgRating", passenger_data));
                map.put("PPhone", generalFun.getJsonValueStr("vPhone", passenger_data));
                map.put("PPhoneC", generalFun.getJsonValueStr("vPhoneCode", passenger_data));
                map.put("PAppVersion", generalFun.getJsonValueStr("iAppVersion", passenger_data));
                map.put("TripId", generalFun.getJsonValueStr("iTripId", last_trip_data));
                map.put("DestLocLatitude", generalFun.getJsonValueStr("tEndLat", last_trip_data));
                map.put("DestLocLongitude", generalFun.getJsonValueStr("tEndLong", last_trip_data));
                map.put("DestLocAddress", generalFun.getJsonValueStr("tDaddress", last_trip_data));
                map.put("REQUEST_TYPE", generalFun.getJsonValueStr("eType", last_trip_data));
                map.put("eFareType", generalFun.getJsonValueStr("eFareType", last_trip_data));
                map.put("iTripId", generalFun.getJsonValueStr("iTripId", last_trip_data));
                map.put("fVisitFee", generalFun.getJsonValueStr("fVisitFee", last_trip_data));
                map.put("eHailTrip", generalFun.getJsonValueStr("eHailTrip", last_trip_data));
                map.put("iActive", generalFun.getJsonValueStr("iActive", last_trip_data));
                map.put("eTollSkipped", generalFun.getJsonValueStr("eTollSkipped", last_trip_data));

                map.put("vVehicleType", generalFun.getJsonValueStr("vVehicleType", last_trip_data));
                map.put("vVehicleType", generalFun.getJsonValueStr("eIconType", last_trip_data));


                map.put("eAfterUpload", generalFun.getJsonValueStr("eAfterUpload", last_trip_data));
                map.put("eBeforeUpload", generalFun.getJsonValueStr("eBeforeUpload", last_trip_data));

                map.put("vDeliveryConfirmCode", generalFun.getJsonValueStr("vDeliveryConfirmCode", last_trip_data));
                map.put("SITE_TYPE", generalFun.getJsonValueStr("SITE_TYPE", userProfileJsonObj));

                if (generalFun.getJsonValueStr("tUserComment", last_trip_data) != null && !generalFun.getJsonValueStr("tUserComment", last_trip_data).equalsIgnoreCase("")) {
                    map.put("tUserComment", generalFun.getJsonValueStr("tUserComment", last_trip_data));
                }

                if (vTripStatus.contains("Not Active") && lastTripExist == true) {
                    // Open rating page
                    bn.putSerializable("TRIP_DATA", map);

                    String ePaymentCollect = generalFun.getJsonValueStr("ePaymentCollect", last_trip_data);
                    if (ePaymentCollect.equals("No")) {
                        new StartActProcess(mContext).startActWithData(CollectPaymentActivity.class, bn);
                    } else {
                        new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
                    }

                } else {

                    if (vTripStatus.contains("Arrived")) {


                        // Open active trip page
                        map.put("vTripStatus", "Arrived");
                        bn.putString("tripDetailArray", last_trip_data.toString());
                        bn.putSerializable("TRIP_DATA", map);
                        bn.putBoolean("isnotification", isnotification);

                        new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                    } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("On Going Trip")) {
                        // Open active trip page


                        map.put("vTripStatus", "EN_ROUTE");
                        bn.putSerializable("TRIP_DATA", map);
                        bn.putString("tripDetailArray", last_trip_data.toString());
                        bn.putBoolean("isnotification", isnotification);
                        new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                    } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("Active")) {
                        // Open cubejek arrived page

                        bn.putSerializable("TRIP_DATA", map);
                        bn.putBoolean("isnotification", isnotification);
                        bn.putString("tripDetailArray", last_trip_data.toString());
                        new StartActProcess(mContext).startActWithData(DriverArrivedActivity.class, bn);
                    }
                }

            } else {

                String eStatus = generalFun.getJsonValueStr("eStatus", userProfileJsonObj);

                if (eStatus.equalsIgnoreCase("suspend")) {
                    new StartActProcess(mContext).startAct(SuspendedDriver_Activity.class);
                } else {
                    new StartActProcess(mContext).startActWithData(MainActivity.class, bn);

                }
            }

        } else {      // bn.putString("USER_PROFILE_JSON", responseString);
            bn.putString("IsAppReStart", "true"); // flag for retrieving data to en route trip pages


            // String last_trip_data = generalFun.getJsonValue("TripDetails", userProfileJsonObj);
            // JSONObject last_trip_data = generalFun.getJsonObject("TripDetails", userProfileJsonObj);


            try {


                passenger_data = generalFun.getJsonArray("PassengerDetails", userProfileJsonObj);
                last_trip_data = generalFun.getJsonArray("TripDetails", userProfileJsonObj);
            } catch (Exception ex) {
                //last_trip_data = generalFun.getJsonObject("TripDetails", userProfileJsonObj);
                // passenger_data = generalFun.getJsonObject("PassengerDetails", userProfileJsonObj);
            }


            // String passenger_data = generalFun.getJsonValue("PassengerDetails", userProfileJsonObj);

            map = new HashMap<>();

            map.put("TotalSeconds", generalFun.getJsonValueStr("TotalSeconds", userProfileJsonObj));
            map.put("TimeState", generalFun.getJsonValueStr("TimeState", userProfileJsonObj));
            map.put("iTripTimeId", generalFun.getJsonValueStr("iTripTimeId", userProfileJsonObj));
            map.put("drivervName", generalFun.getJsonValue("vName", responseString));
            map.put("drivervLastName", generalFun.getJsonValue("vLastName", responseString));
            map.put("Message", "CabRequested");
            location = tracker.getLocation();
            try {

                if (position != null) {
                    setdata(Integer.parseInt(position));
                } else {

                    if (last_trip_data.length() > 1) {
                        for (int p = 0; p < last_trip_data.length(); p++) {
                            String latitudeEnd;
                            String longitudeEnd;
                            String latitudeStart;
                            String longitudeStart;
                            float distance;

                            String status;
                            latitudeEnd = generalFun.getJsonValueStr("tEndLat", last_trip_data.getJSONObject(p));
                            longitudeEnd = generalFun.getJsonValueStr("tEndLong", last_trip_data.getJSONObject(p));
                            latitudeStart = generalFun.getJsonValueStr("tStartLat", last_trip_data.getJSONObject(p));
                            longitudeStart = generalFun.getJsonValueStr("tStartLong", last_trip_data.getJSONObject(p));
                            status = generalFun.getJsonValueStr("iActive", last_trip_data.getJSONObject(p));
                            if (status.equalsIgnoreCase("On Going Trip")) {
                                Location end_location = new Location("locationA");
                                end_location.setLatitude(Double.parseDouble(latitudeEnd));
                                end_location.setLongitude(Double.parseDouble(longitudeEnd));

                                distance = location.distanceTo(end_location);

                                    checdistance(distance, p);

                            } else if (status.equalsIgnoreCase("Canceled") || status.equalsIgnoreCase("Finished")) {
                                if (p == last_trip_data.length() - 1 && last_trip_data.length() > 1) {
                                    setdata(0);
                                } else {
                                    if (j == last_trip_data.length() - 1) {
                                        bn.putSerializable("TRIP_DATA", map);
                                        bn.putString("tripDetailArray", last_trip_data.toString());
                                        new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
                                    }
                                    j++;
                                }
                            } else {
                                Location start_location = new Location("locationA");
                                start_location.setLatitude(Double.parseDouble(latitudeStart));
                                start_location.setLongitude(Double.parseDouble(longitudeStart));

                                distance = location.distanceTo(start_location);
                                checdistance(distance, p);
                            }


                        }


                    } else {
                        if (last_trip_data.length() == 1) {
                            setdata(0);
                        } else {

                        }

                    }
                }
            } catch (Exception ex) {
                vTripStatus = generalFun.getJsonValueStr("vTripStatus", userProfileJsonObj);
                if (vTripStatus.contains("Not Active") && lastTripExist == true) {
                    // Open rating page

                    //putInMap(positionSmallestDist);

                    bn.putSerializable("TRIP_DATA", map);
                    bn.putString("tripDetailArray", last_trip_data.toString());
                    //  if (!ePaymentCollect.equals("Settelled")) {
                    new StartActProcess(mContext).startActWithData(CollectPaymentActivity.class, bn);
//                   } else {
//
//                           new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
//
//                   }
                } else {
                    if (vTripStatus.contains("Arrived")) {
                        // putInMap(i);
                        // Open active trip page
                        map.put("vTripStatus", "Arrived");
                        bn.putSerializable("TRIP_DATA", map);
                        bn.putBoolean("isnotification", isnotification);
                        bn.putString("tripDetailArray", last_trip_data.toString());
                        new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                    } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("On Going Trip")) {
                        // Open active trip page
                        // putInMap(i);
                        map.put("vTripStatus", "EN_ROUTE");
                        bn.putSerializable("TRIP_DATA", map);
                        bn.putBoolean("isnotification", isnotification);
                        bn.putString("tripDetailArray", last_trip_data.toString());
                        new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                    } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("Active")) {
                        // Open cubejek arrived page
                        //   putInMap(i);
                        bn.putSerializable("TRIP_DATA", map);
                        bn.putBoolean("isnotification", isnotification);
                        bn.putString("tripDetailArray", last_trip_data.toString());
                        new StartActProcess(mContext).startActWithData(DriverArrivedActivity.class, bn);
                    }
                }
            }
        }
        // ActivityCompat.finishAffinity((Activity) mContext);
    }

    public void setdata(int i) {
        try {
            vTripStatus = generalFun.getJsonValueStr("iActive", last_trip_data.getJSONObject(i));
        } catch (Exception ex) {
        }
        if (vTripStatus.contains("Not Active") && lastTripExist == true) {
            // Open rating page
            putInMap(i);
            bn.putSerializable("TRIP_DATA", map);
            String ePaymentCollect = null;
            try {
                ePaymentCollect = generalFun.getJsonValueStr("eDriverPaymentStatus", last_trip_data.getJSONObject(i));
            } catch (Exception ex) {
            }

            if (!ePaymentCollect.equals("Settelled")) {
                new StartActProcess(mContext).startActWithData(CollectPaymentActivity.class, bn);
            } else {
                if (i == last_trip_data.length() - 1) {
                    new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
                }
            }
        } else {
            if (vTripStatus.contains("Arrived")) {
                putInMap(i);
                // Open active trip page
                map.put("vTripStatus", "Arrived");
                bn.putSerializable("TRIP_DATA", map);
                bn.putBoolean("isnotification", isnotification);
                bn.putString("tripDetailArray", last_trip_data.toString());
                new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
            } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("On Going Trip")) {
                // Open active trip page
                putInMap(i);
                map.put("vTripStatus", "EN_ROUTE");
                bn.putSerializable("TRIP_DATA", map);
                bn.putBoolean("isnotification", isnotification);
                bn.putString("tripDetailArray", last_trip_data.toString());
                new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
            } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("Active")) {
                // Open cubejek arrived page
                putInMap(i);
                bn.putSerializable("TRIP_DATA", map);
                bn.putBoolean("isnotification", isnotification);
                bn.putString("tripDetailArray", last_trip_data.toString());
                new StartActProcess(mContext).startActWithData(DriverArrivedActivity.class, bn);
            } else {

                    putInMap(0);
                    bn.putSerializable("TRIP_DATA", map);
                    bn.putString("tripDetailArray", last_trip_data.toString());
                    new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);


            }
        }
    }


    private void checdistance(float distance, int p) {

        if (check_distance == 0) {
            check_distance = distance;
            positionSmallestDist = p;
        } else {
            if (check_distance >= distance) {
                positionSmallestDist = p;
            } else {

            }
        }

        if (p == last_trip_data.length() - 1) {
            setdata(positionSmallestDist);
        }
    }

    public void putInMap(int i) {
        try {
            map.put("sourceLatitude", generalFun.getJsonValueStr("tStartLat", last_trip_data.getJSONObject(i)));
            map.put("sourceLongitude", generalFun.getJsonValueStr("tStartLong", last_trip_data.getJSONObject(i)));
            map.put("tSaddress", generalFun.getJsonValueStr("tSaddress", last_trip_data.getJSONObject(i)));
            map.put("PassengerId", generalFun.getJsonValueStr("iUserId", last_trip_data.getJSONObject(i)));
            map.put("PName", generalFun.getJsonValueStr("vName", last_trip_data.getJSONObject(i).getJSONObject("PassengerDetails")));
            map.put("PPicName", generalFun.getJsonValueStr("vImgName", last_trip_data.getJSONObject(i).getJSONObject("PassengerDetails")));
            map.put("PFId", generalFun.getJsonValueStr("vFbId", last_trip_data.getJSONObject(i).getJSONObject("PassengerDetails")));
            map.put("PRating", generalFun.getJsonValueStr("vAvgRating", last_trip_data.getJSONObject(i).getJSONObject("PassengerDetails")));
            map.put("PPhone", generalFun.getJsonValueStr("vPhone", last_trip_data.getJSONObject(i).getJSONObject("PassengerDetails")));
            map.put("PPhoneC", generalFun.getJsonValueStr("vPhoneCode", last_trip_data.getJSONObject(i).getJSONObject("PassengerDetails")));
            map.put("PAppVersion", generalFun.getJsonValueStr("iAppVersion", last_trip_data.getJSONObject(i).getJSONObject("PassengerDetails")));
            map.put("TripId", generalFun.getJsonValueStr("iTripId", last_trip_data.getJSONObject(i)));
            map.put("DestLocLatitude", generalFun.getJsonValueStr("tEndLat", last_trip_data.getJSONObject(i)));
            map.put("DestLocLongitude", generalFun.getJsonValueStr("tEndLong", last_trip_data.getJSONObject(i)));
            map.put("DestLocAddress", generalFun.getJsonValueStr("tDaddress", last_trip_data.getJSONObject(i)));
            map.put("REQUEST_TYPE", generalFun.getJsonValueStr("eType", last_trip_data.getJSONObject(i)));
            map.put("eFareType", generalFun.getJsonValueStr("eFareType", last_trip_data.getJSONObject(i)));
            map.put("iTripId", generalFun.getJsonValueStr("iTripId", last_trip_data.getJSONObject(i)));
            map.put("fVisitFee", generalFun.getJsonValueStr("fVisitFee", last_trip_data.getJSONObject(i)));
            map.put("eHailTrip", generalFun.getJsonValueStr("eHailTrip", last_trip_data.getJSONObject(i)));
            map.put("iActive", generalFun.getJsonValueStr("iActive", last_trip_data.getJSONObject(i)));
            map.put("eTollSkipped", generalFun.getJsonValueStr("eTollSkipped", last_trip_data.getJSONObject(i)));
            map.put("vVehicleType", generalFun.getJsonValueStr("vVehicleType", last_trip_data.getJSONObject(i)));
            map.put("vVehicleType", generalFun.getJsonValueStr("eIconType", last_trip_data.getJSONObject(i)));
            map.put("eAfterUpload", generalFun.getJsonValueStr("eAfterUpload", last_trip_data.getJSONObject(i)));
            map.put("eBeforeUpload", generalFun.getJsonValueStr("eBeforeUpload", last_trip_data.getJSONObject(i)));
            map.put("vDeliveryConfirmCode", generalFun.getJsonValueStr("vDeliveryConfirmCode", last_trip_data.getJSONObject(i)));
            map.put("SITE_TYPE", generalFun.getJsonValueStr("SITE_TYPE", userProfileJsonObj));
            if (generalFun.getJsonValueStr("tUserComment", last_trip_data.getJSONObject(i)) != null && !generalFun.getJsonValueStr("tUserComment", last_trip_data.getJSONObject(i)).equalsIgnoreCase("")) {
                map.put("tUserComment", generalFun.getJsonValueStr("tUserComment", last_trip_data.getJSONObject(i)));
            }
        } catch (Exception ex) {

        }
    }

    public void setGeneralData() {
        generalFun.storedata(Utils.ENABLE_PUBNUB_KEY, generalFun.getJsonValueStr("ENABLE_PUBNUB", userProfileJsonObj));
        generalFun.storedata(Utils.SESSION_ID_KEY, generalFun.getJsonValueStr("tSessionId", userProfileJsonObj));
        generalFun.storedata(Utils.DEVICE_SESSION_ID_KEY, generalFun.getJsonValueStr("tDeviceSessionId", userProfileJsonObj));
        generalFun.storedata(Utils.FETCH_TRIP_STATUS_TIME_INTERVAL_KEY, generalFun.getJsonValueStr("FETCH_TRIP_STATUS_TIME_INTERVAL", userProfileJsonObj));
        generalFun.storedata(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, userProfileJsonObj));
        generalFun.storedata(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, userProfileJsonObj));
        generalFun.storedata(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, userProfileJsonObj));
        generalFun.storedata(CommonUtilities.PUBNUB_PUB_KEY, generalFun.getJsonValueStr("PUBNUB_PUBLISH_KEY", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.PUBNUB_SUB_KEY, generalFun.getJsonValueStr("PUBNUB_SUBSCRIBE_KEY", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.PUBNUB_SEC_KEY, generalFun.getJsonValueStr("PUBNUB_SECRET_KEY", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.SITE_TYPE_KEY, generalFun.getJsonValueStr("SITE_TYPE", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.MOBILE_VERIFICATION_ENABLE_KEY, generalFun.getJsonValueStr("MOBILE_VERIFICATION_ENABLE", userProfileJsonObj));
        generalFun.storedata("LOCATION_ACCURACY_METERS", generalFun.getJsonValueStr("LOCATION_ACCURACY_METERS", userProfileJsonObj));
        generalFun.storedata("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFun.getJsonValueStr("DRIVER_LOC_UPDATE_TIME_INTERVAL", userProfileJsonObj));
        generalFun.storedata("RIDER_REQUEST_ACCEPT_TIME", generalFun.getJsonValueStr("RIDER_REQUEST_ACCEPT_TIME", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, generalFun.getJsonValueStr(CommonUtilities.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, userProfileJsonObj));
        generalFun.storedata(CommonUtilities.ENABLE_TOLL_COST, generalFun.getJsonValueStr("ENABLE_TOLL_COST", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.TOLL_COST_APP_ID, generalFun.getJsonValueStr("TOLL_COST_APP_ID", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.TOLL_COST_APP_CODE, generalFun.getJsonValueStr("TOLL_COST_APP_CODE", userProfileJsonObj));
        generalFun.storedata(Utils.ENABLE_PUBNUB_KEY, generalFun.getJsonValueStr("ENABLE_PUBNUB", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.WALLET_ENABLE, generalFun.getJsonValueStr("WALLET_ENABLE", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.REFERRAL_SCHEME_ENABLE, generalFun.getJsonValueStr("REFERRAL_SCHEME_ENABLE", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.APP_DESTINATION_MODE, generalFun.getJsonValueStr("APP_DESTINATION_MODE", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.APP_TYPE, generalFun.getJsonValueStr("APP_TYPE", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.HANDICAP_ACCESSIBILITY_OPTION, generalFun.getJsonValueStr("HANDICAP_ACCESSIBILITY_OPTION", userProfileJsonObj));
        generalFun.storedata(CommonUtilities.FEMALE_RIDE_REQ_ENABLE, generalFun.getJsonValueStr("FEMALE_RIDE_REQ_ENABLE", userProfileJsonObj));
    }

    @Override
    public void onLocationUpdate(Location location) {
        this.location = location;
    }
}