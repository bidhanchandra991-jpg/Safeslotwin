package com.team.eleven.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.team.eleven.APICallingPackage.Class.APIRequestManager;
import com.team.eleven.APICallingPackage.Interface.ResponseManager;
import com.team.eleven.R;
import com.team.eleven.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import static com.team.eleven.APICallingPackage.Class.Validations.ShowToast;
import static com.team.eleven.APICallingPackage.Config.ADDAMOUNT;
import static com.team.eleven.APICallingPackage.Constants.ADDAMOUNTTYPE;

public class PaytmActivity extends AppCompatActivity implements ResponseManager {

    private String orderID = "";
    private String customerID = "";
    private String PayAmount = "0.0";

    PaytmActivity activity;
    Context context;
    ImageView im_back;
    TextView tv_HeaderName, tv_totalamount, tv_Proceed;
    SessionManager sessionManager;
    WebView webView;
    ScrollView scroll_form; // ফরমটা লুকানোর জন্য
    ProgressBar progressBar;

    ResponseManager responseManager;
    APIRequestManager apiRequestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paytm);
        context = activity = this;
        sessionManager = new SessionManager();
        responseManager = this;
        apiRequestManager = new APIRequestManager(activity);

        // UI Setup
        im_back = findViewById(R.id.im_back);
        tv_HeaderName = findViewById(R.id.tv_HeaderName);
        tv_HeaderName.setText("KUKUPAY PAYMENT");
        
        // XML এর আইডিগুলো কানেক্ট করা
        tv_totalamount = findViewById(R.id.tv_totalamount);
        tv_Proceed = findViewById(R.id.tv_Checkout); // তোর XML এ এটার নাম tv_Checkout ছিল
        scroll_form = findViewById(R.id.scroll_form); 
        webView = findViewById(R.id.payment_webview);
        progressBar = findViewById(R.id.progressBar);

        webView.getSettings().setJavaScriptEnabled(true);

        im_back.setOnClickListener(v -> onBackPressed());

        // ১. টাকাটা আগের পেজ থেকে নিয়ে TextView তে দেখানো
        PayAmount = getIntent().getStringExtra("FinalAmount");
        if(tv_totalamount != null) {
            tv_totalamount.setText("₹ " + PayAmount);
        }

        customerID = sessionManager.getUser(context).getUser_id();
        orderID = "ORD" + System.currentTimeMillis();

        // ২. ইউজার যখন "Checkout" বাটনে ক্লিক করবে, তখনই পেমেন্ট লিঙ্ক জেনারেট হবে
        tv_Proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchKukuPayLink();
            }
        });
    }

    private void fetchKukuPayLink() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        ShowToast(context, "পেমেন্ট লিঙ্ক তৈরি হচ্ছে...");

        String kukuPayApiUrl = "https://kukupay.pro/pay/create";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("api_key", "zec8cLsWgDcToqIPjF1YxnbjDdduunkq");
            requestBody.put("amount", PayAmount); // এই সেই সিলেক্ট করা টাকা
            requestBody.put("phone", sessionManager.getUser(context).getMobile()); 
            requestBody.put("webhook_url", "http://airtelwin.dpdns.org/webhook.php");
            requestBody.put("return_url", "http://airtelwin.dpdns.org/success");
            requestBody.put("order_id", orderID);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, kukuPayApiUrl, requestBody,
                response -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getInt("status") == 200) {
                            String url = response.getString("payment_url");
                            loadPaymentPage(url);
                        }
                    } catch (Exception e) { ShowToast(context, "লিঙ্ক এরর"); }
                }, error -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    ShowToast(context, "সার্ভার কানেকশন এরর");
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void loadPaymentPage(String url) {
        // পেমেন্ট শুরু হলে ফরম লুকিয়ে WebView দেখানো
        if (scroll_form != null) scroll_form.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("/success")) {
                    webView.setVisibility(View.GONE);
                    ShowToast(context, "পেমেন্ট সফল হয়েছে!");
                    callAddAmount(true);
                }
            }
        });
        webView.loadUrl(url);
    }

    private void callAddAmount(boolean isShowLoader) {
        try {
            apiRequestManager.callAPI(ADDAMOUNT, createRequestJson(), context, activity, ADDAMOUNTTYPE, isShowLoader, responseManager);
        } catch (JSONException e) { e.printStackTrace(); }
    }

    JSONObject createRequestJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", customerID);
            jsonObject.put("amount", PayAmount);
            jsonObject.put("mode", "KukuPay");
            jsonObject.put("order_id", orderID);
        } catch (JSONException e) { e.printStackTrace(); }
        return jsonObject;
    }

    @Override
    public void getResult(Context mContext, String type, String message, JSONObject result) {
        ShowToast(context, "ব্যালেন্স অ্যাড হয়েছে!");
        finish();
    }

    @Override
    public void onError(Context mContext, String type, String message) {
        ShowToast(context, "ব্যালেন্স আপডেট করতে সমস্যা হয়েছে");
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE);
            if (scroll_form != null) scroll_form.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
