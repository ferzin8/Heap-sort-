/*
 * *
 *  * Created by Evgeniy Fersman on 21.04.20 17:50
 *  * Copyright (c) 2020 . All rights reserved.
 *  * Last modified 21.04.20 16:06
 *
 */

package com.supplerus.astrocode.fragments.screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;
import com.supplerus.astrocode.JSONParsers.PeopleObject;
import com.supplerus.astrocode.R;
import com.supplerus.astrocode.activity.MainActivity;
import com.supplerus.astrocode.fragments.rootFragments.ACFragment;
import com.supplerus.astrocode.interfaces.CompleteBlock;
import com.supplerus.astrocode.managers.advices.AdviceObject;
import com.supplerus.astrocode.managers.api.ApiHelper;
import com.supplerus.astrocode.managers.api.ApiHelperBilling;
import com.supplerus.astrocode.managers.api.ApiHelperVersion;
import com.supplerus.astrocode.managers.billings.BillingManager;
import com.supplerus.astrocode.managers.billings.PaymentHistoryManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;

import static com.supplerus.astrocode.activity.MainActivity.mainActivity;

public class BillingInfoFragment extends ACFragment {
    private ProgressBar progressBar;

    private Button buyConsultation;
    private Button subscribe;
    private Button cancel;

    private TextView consultationDescription;
    private TextView subscriptionDescription;
    private TextView policy;

    private AdviceObject adviceObject;
    private PeopleObject peopleObject;

    public BillingInfoFragment() {
        super();
        super.fragmentName = "BuyAdviceScreen";
        showBottomMenu = false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_billing_info, null);

        return rootView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);

        RelativeLayout currentLayout = rootView.findViewById(R.id.buyAdviceMain);
        consultationDescription = rootView.findViewById(R.id.purchaseDescription);
        buyConsultation         = rootView.findViewById(R.id.buyOnce);
        subscriptionDescription = rootView.findViewById(R.id.subscriptionDescription);
        subscribe               = rootView.findViewById(R.id.buySubscription);
        cancel                  = rootView.findViewById(R.id.cancelPurchase);
        progressBar             = rootView.findViewById(R.id.contentLoaderBillingInfo);
        policy                  = rootView.findViewById(R.id.purchasePolicy);

        Objects.requireNonNull(styleObjectHashMap.get("content_view")).setToView(currentLayout);

        buyConsultation.setText(Objects.requireNonNull(contentObjectHashMap.get("buyProductButton_title")).getTextString());
        Objects.requireNonNull(styleObjectHashMap.get("content_view_buyProductButton_title")).setToView(buyConsultation);

        subscribe.setText(Objects.requireNonNull(contentObjectHashMap.get("buySubscriptionButton_title")).getTextString());
        Objects.requireNonNull(styleObjectHashMap.get("content_view_buySubscriptionButton_title")).setToView(subscribe);

        cancel.setText(Objects.requireNonNull(contentObjectHashMap.get("cancelButton_title")).getTextString());
        Objects.requireNonNull(styleObjectHashMap.get("content_view_cancelButton_title")).setToView(cancel);

        policy.setText(Objects.requireNonNull(contentObjectHashMap.get("policy_title")).getTextString());
        Objects.requireNonNull(styleObjectHashMap.get("content_view_policy_title")).setToView(policy);
        if (Build.VERSION.SDK_INT >= 26) {
            policy.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        String policyTitle = "";
        String licenceTitle = "";

        int policyIndex = 0;
        int licenceIndex = 0;

        if (Objects.requireNonNull(contentObjectHashMap.get("policy_title")).texts.size() > 1) {
            policyTitle = Objects.requireNonNull(contentObjectHashMap.get("policy_title")).texts.get(1);
        }
        if (Objects.requireNonNull(contentObjectHashMap.get("policy_title")).texts.size() > 2) {
            licenceTitle = Objects.requireNonNull(contentObjectHashMap.get("policy_title")).texts.get(2);
        }

        int policyTitleLength = policyTitle.length();
        int licenceTitleLength = licenceTitle.length();

        if (policy.getText().toString().contains(policyTitle)) {
            policyIndex = policy.getText().toString().indexOf(policyTitle);
        }
        if (policy.getText().toString().contains(licenceTitle)) {
            licenceIndex = policy.getText().toString().indexOf(licenceTitle);
        }

        int finalPolicyIndex = policyIndex;
        int finalLicenceIndex = licenceIndex;

        ApiHelperVersion.getVersionCheck(new CompleteBlock() {
            @Override
            public void complete(JSONObject jsonObject, ApiHelper.ACLoaderConnectionError errorCode) {
                if (errorCode == ApiHelper.ACLoaderConnectionError.LFLoaderConnectionErrorNone) {
                    String policyURL = "";
                    String licenceURL = "";
                    try {
                        if (jsonObject.has("result") && jsonObject.getJSONObject("result").has("valid") && jsonObject.getJSONObject("result").getBoolean("valid")) {
                            licenceURL = jsonObject.getJSONObject("result").has("license_url") ? jsonObject.getJSONObject("result").getString("license_url") : "";
                            policyURL  = jsonObject.getJSONObject("result").has("policy_url")  ? jsonObject.getJSONObject("result").getString("policy_url")  : "";
                        }

                        SpannableString ss = new SpannableString(policy.getText().toString());
                        String finalPolicyURL = policyURL;
                        String finalLicenceURL = licenceURL;

                        ClickableSpan policyClickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(@NotNull View textView) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(finalPolicyURL));
                                startActivity(i);
                            }

                            @Override
                            public void updateDrawState(@NotNull TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(true);
                                ds.setColor(Color.BLACK);
                            }
                        };

                        ClickableSpan licenceClickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(@NotNull View textView) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(finalLicenceURL));
                                startActivity(i);
                            }

                            @Override
                            public void updateDrawState(@NotNull TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setUnderlineText(true);
                                ds.setColor(Color.BLACK);
                            }
                        };

                        ss.setSpan(licenceClickableSpan, finalLicenceIndex, finalLicenceIndex + licenceTitleLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ss.setSpan(policyClickableSpan,  finalPolicyIndex,  finalPolicyIndex  + policyTitleLength,  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        policy.setText(ss);
                        policy.setMovementMethod(LinkMovementMethod.getInstance());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Objects.requireNonNull(styleObjectHashMap.get("content_view_detailProduct_title")).setToView(consultationDescription);
        Objects.requireNonNull(styleObjectHashMap.get("content_view_detailSubscription_title")).setToView(subscriptionDescription);

        FrameLayout.LayoutParams currentLayoutParams = (FrameLayout.LayoutParams) currentLayout.getLayoutParams();
        currentLayoutParams.topMargin += (int)(1.1f * getTopSpace());
        currentLayoutParams.bottomMargin += getBottomSpace();
        currentLayout.setLayoutParams(currentLayoutParams);

        getSkuParamsOneTimePurchase();
        getSubscriptionInfo();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.hideExtraFragment();
            }
        });
    }

    public void setAdviceObject (AdviceObject adviceObject) {
        this.adviceObject = adviceObject;
    }

    public void setPeopleObject (PeopleObject peopleObject) {this.peopleObject = peopleObject;}

    private AdviceObject getAdviceObject (){
        return adviceObject;
    }

    private PeopleObject getPeopleObject () {
        return peopleObject;
    }

    public void getSubscriptionInfo () {
        ApiHelperBilling.getSubscription(new CompleteBlock() {
            @Override
            public void complete(JSONObject jsonObject, ApiHelper.ACLoaderConnectionError errorCode) {
                if (errorCode == ApiHelper.ACLoaderConnectionError.LFLoaderConnectionErrorNone) {
                    try {
                        String subc = jsonObject.getJSONObject("result").getString("android");
                        Objects.requireNonNull(BillingManager.sharedInstance()).getSkuID(subc, BillingClient.SkuType.SUBS,
                                new BillingManager.BillingInterface() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void billingComplete(SkuDetails skuDetails, BillingManager.BillingError billingError, String message) {
                                if (billingError == BillingManager.BillingError.NONE) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    subscriptionDescription.setText(skuDetails.getTitle() + "\n" + skuDetails.getDescription() + "\n" + "(" +
                                            ((double) skuDetails.getPriceAmountMicros()/(double) 1000000) + " " +
                                            Objects.requireNonNull(PaymentHistoryManager.sharedInstance()).getCurrencyImage(skuDetails.getPriceCurrencyCode())
                                            + ")" + "\n" + getSubscriptionPeriod(skuDetails.getSubscriptionPeriod()));
                                    subscribe.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            v.setClickable(false);
                                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                    .setSkuDetails(skuDetails)
                                                    .build();
                                            Objects.requireNonNull(BillingManager.sharedInstance()).billingClient.launchBillingFlow((MainActivity)mainActivity, flowParams);
                                            if (peopleObject == null)
                                                BillingManager.sharedInstance().getBillingInfo(getAdviceObject().adviceId, subc, "", BillingClient.SkuType.SUBS, skuDetails);
                                            else
                                                BillingManager.sharedInstance().getBillingInfo(getAdviceObject().adviceId, subc, getPeopleObject().partnerId, BillingClient.SkuType.SUBS, skuDetails);
                                            v.setClickable(true);
                                            cancel.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String getSubscriptionPeriod (String s) {
        if ("P1M".equals(s))
            return "1 месяц";
        return s;
    }

    private void getSkuParamsOneTimePurchase () {
        Objects.requireNonNull(BillingManager.sharedInstance()).getSkuID(getAdviceObject().androidBilling,
            BillingClient.SkuType.INAPP, new BillingManager.BillingInterface() {
                @SuppressLint("SetTextI18n")
                @Override
                public void billingComplete(SkuDetails skuDetails, BillingManager.BillingError billingError, String message) {
                    if (billingError == BillingManager.BillingError.NONE) {
                        progressBar.setVisibility(View.INVISIBLE);
                        consultationDescription.setText(getAdviceObject().adviceName + "\n" + skuDetails.getDescription());
                        buyConsultation.setText(((double) skuDetails.getPriceAmountMicros()/(double) 1000000) + " " +
                                Objects.requireNonNull(PaymentHistoryManager.sharedInstance()).getCurrencyImage(skuDetails.getPriceCurrencyCode()));

                        buyConsultation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                v.setClickable(false);
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetails)
                                                .build();
                                Objects.requireNonNull(BillingManager.sharedInstance()).billingClient.launchBillingFlow((MainActivity)mainActivity, flowParams);
                                if (peopleObject == null)
                                    BillingManager.sharedInstance().getBillingInfo(getAdviceObject().adviceId,
                                            getAdviceObject().androidBilling, "",
                                            BillingClient.SkuType.INAPP, skuDetails);
                                else
                                    BillingManager.sharedInstance().getBillingInfo(getAdviceObject().adviceId,
                                            getAdviceObject().androidBilling, getPeopleObject().partnerId,
                                            BillingClient.SkuType.INAPP, skuDetails);
                                v.setClickable(true);
                                cancel.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
            });
    }
}