package com.jet.driver;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.pinnedListView.CategoryListItem;
import com.view.pinnedListView.PinnedCategorySectionListAdapter;
import com.view.pinnedListView.PinnedSectionListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UfxCategoryActivity extends AppCompatActivity implements PinnedCategorySectionListAdapter.CountryClick {


    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    ProgressBar loading;

    ErrorView errorView;


    String next_page_str = "";

    ArrayList<CategoryListItem> categoryitems_list;
    PinnedCategorySectionListAdapter pinnedSectionListAdapter;
    PinnedSectionListView category_list;
    private CategoryListItem[] sections;
    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;
    String UBERX_PARENT_CAT_ID = "";
    MTextView introTxt;

    String app_type = "Ride";

    public String userProfileJson = "";

    private JSONObject obj_userProfile;
    MTextView noResTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ufx_category);

        generalFunc = new GeneralFunctions(getActContext());


        obj_userProfile = generalFunc.getJsonObject(userProfileJson);
//        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        app_type = generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        introTxt = (MTextView) findViewById(R.id.introTxt);
        noResTxt = (MTextView) findViewById(R.id.noResTxt);

        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        category_list = (PinnedSectionListView) findViewById(R.id.category_list);
        category_list.setShadowVisible(true);
        UBERX_PARENT_CAT_ID = getIntent().getStringExtra("UBERX_PARENT_CAT_ID");

        backImgView.setOnClickListener(new setOnClickList());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            category_list.setFastScrollEnabled(false);
            category_list.setFastScrollAlwaysVisible(false);
        }

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANANGE_SERVICES"));

        introTxt.setText(generalFunc.retrieveLangLBl("Select category below to add services you are going to provide", "LBL_MANAGE_SERVICE_INTRO_TXT"));

        categoryitems_list = new ArrayList<>();
        getCategoryList(false);


        category_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {

                    mIsLoading = true;
                    //category_list.addFooterView();
                    loading.setVisibility(View.VISIBLE);

                    getCategoryList(true);

                } else if (isNextPageAvailable == false) {
                    loading.setVisibility(View.GONE);
                }

            }
        });


    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {


            if (view == backImgView) {
                onBackPressed();
            }

        }
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        loading.setVisibility(View.GONE);
    }


    public Context getActContext() {
        return UfxCategoryActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Select Category", "LBL_SELECT_CATEGORY"));
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }


    int sectionPosition = 0, listPosition = 0;

    public void getCategoryList(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getvehicleCategory");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iVehicleCategoryId", UBERX_PARENT_CAT_ID);
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }


        // noResTxt.setVisibility(View.GONE);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                //noResTxt.setVisibility(View.GONE);

                if (responseString != null && !responseString.equals("")) {

                    closeLoader();


                    if (generalFunc.checkDataAvail(CommonUtilities.action_str, responseString) == true) {
                        sections = new CategoryListItem[generalFunc.getJsonArray(CommonUtilities.message_str, responseString).length()];

                        //categoryitems_list.clear();

                        String nextPage = generalFunc.getJsonValue("NextPage", responseString);
                        if (!UBERX_PARENT_CAT_ID.equalsIgnoreCase("0")) {
                            JSONArray mainListArr = generalFunc.getJsonArray(CommonUtilities.message_str, responseString);
                            if (pinnedSectionListAdapter == null) {
                                pinnedSectionListAdapter = new PinnedCategorySectionListAdapter(getActContext(), categoryitems_list, sections);
                                category_list.setAdapter(pinnedSectionListAdapter);
                            }

                            pinnedSectionListAdapter.setCountryClickListener(UfxCategoryActivity.this);


                            for (int j = 0; j < mainListArr.length(); j++) {

                                CategoryListItem section = new CategoryListItem(CategoryListItem.SECTION, "0");
                                section.sectionPosition = sectionPosition;
                                section.listPosition = listPosition++;
                                section.CountSubItems = generalFunc.parseIntegerValue(0, j + "");
                                onSectionAdded(section, sectionPosition);

                                JSONObject subTempJson = generalFunc.getJsonObject(mainListArr, j);

                                CategoryListItem categoryListItem = new CategoryListItem(CategoryListItem.ITEM, generalFunc.getJsonValue("vTitle", subTempJson.toString()));
                                categoryListItem.sectionPosition = sectionPosition;
                                categoryListItem.listPosition = listPosition++;
                                categoryListItem.setvTitle(generalFunc.getJsonValue("vTitle", subTempJson.toString()));
                                categoryListItem.setiVehicleCategoryId(generalFunc.getJsonValue("iVehicleCategoryId", subTempJson.toString()));

                                categoryitems_list.add(categoryListItem);
                                pinnedSectionListAdapter.notifyDataSetChanged();
                                sectionPosition++;
                            }

                        } else {

                            JSONArray mainListArr = generalFunc.getJsonArray(CommonUtilities.message_str, responseString);
                            sections = new CategoryListItem[mainListArr.length()];

                            if (pinnedSectionListAdapter == null) {
                                pinnedSectionListAdapter = new PinnedCategorySectionListAdapter(getActContext(), categoryitems_list, sections);
                                category_list.setAdapter(pinnedSectionListAdapter);
                            }

                            pinnedSectionListAdapter.setCountryClickListener(UfxCategoryActivity.this);


                            int sectionPosition = 0, listPosition = 0;
                            for (int i = 0; i < mainListArr.length(); i++) {
                                JSONObject tempJson = generalFunc.getJsonObject(mainListArr, i);

                                String iVehicleCategoryId = generalFunc.getJsonValue("iVehicleCategoryId", tempJson.toString());
                                String vCategory = generalFunc.getJsonValue("vCategory", tempJson.toString());

                                CategoryListItem section = new CategoryListItem(CategoryListItem.SECTION, vCategory);
                                section.sectionPosition = sectionPosition;
                                section.listPosition = listPosition++;
                                section.CountSubItems = generalFunc.parseIntegerValue(0, vCategory);
                                Utils.printLog("sectionPosition", "::" + sectionPosition);
                                Utils.printLog("section", "::" + section);
                                onSectionAdded(section, sectionPosition);
                                categoryitems_list.add(section);

                                JSONArray subListArr = generalFunc.getJsonArray("SubCategory", tempJson.toString());

                                for (int j = 0; j < subListArr.length(); j++) {
                                    JSONObject subTempJson = generalFunc.getJsonObject(subListArr, j);

                                    CategoryListItem categoryListItem = new CategoryListItem(CategoryListItem.ITEM, generalFunc.getJsonValue("vCategory", tempJson.toString()));
                                    categoryListItem.sectionPosition = sectionPosition;
                                    categoryListItem.listPosition = listPosition++;
                                    categoryListItem.setvTitle(generalFunc.getJsonValue("vTitle", subTempJson.toString()));
                                    categoryListItem.setiVehicleCategoryId(generalFunc.getJsonValue("iVehicleCategoryId", subTempJson.toString()));

                                    categoryitems_list.add(categoryListItem);
                                    pinnedSectionListAdapter.notifyDataSetChanged();
                                }

                                sectionPosition++;
                            }
                        }

                        if (!nextPage.equals("") && !nextPage.equals("0")) {
                            next_page_str = nextPage;
                            isNextPageAvailable = true;
                        } else {
                            removeNextPageConfig();
                        }
                        pinnedSectionListAdapter.notifyDataSetChanged();
                    } else {
                        introTxt.setVisibility(View.GONE);
                        noResTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(CommonUtilities.message_str, responseString)));
                        noResTxt.setVisibility(View.VISIBLE);

                    }
                } else {
                    generateErrorView();
                }

                mIsLoading = false;
            }
        });
        exeWebServer.execute();
    }


    protected void onSectionAdded(CategoryListItem section, int sectionPosition) {
        sections[sectionPosition] = section;
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
                getCategoryList(false);
            }
        });
    }

    @Override
    public void countryClickList(CategoryListItem countryListItem) {


        Bundle bn = new Bundle();
        bn.putString("iVehicleCategoryId", countryListItem.getiVehicleCategoryId());
        bn.putString("vTitle", countryListItem.getvTitle());
        (new StartActProcess(getActContext())).startActWithData(AddServiceActivity.class, bn);
        Utils.printLog("CategoryListItem::", countryListItem + "");

    }
}
