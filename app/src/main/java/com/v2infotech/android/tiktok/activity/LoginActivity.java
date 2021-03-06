package com.v2infotech.android.tiktok.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;
import com.v2infotech.android.tiktok.R;
import com.v2infotech.android.tiktok.Utils.CircleTransform;
import com.v2infotech.android.tiktok.Utils.CommonMethod;
import com.v2infotech.android.tiktok.Utils.Contants;
import com.v2infotech.android.tiktok.Utils.HttpUtility;
import com.v2infotech.android.tiktok.database.DbHelper;
import com.v2infotech.android.tiktok.model.LoginResponseData;
import com.v2infotech.android.tiktok.model.LoginResponseParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.v2infotech.android.tiktok.Utils.Contants.BASE_URL;
import static com.v2infotech.android.tiktok.Utils.Contants.LOGIN;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView forgot_password_txt;
    private EditText user_name_txt, user_password_txt;
    private ImageView user_profile, facebook, google, twitter;
    private Button login_btn, signup_btn;
    private CheckBox remember_me_checkbox;
    private ProgressDialog pDialog;
    private String mResponce;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    LoginResponseParser loginResponceParser;
    CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    String name,password,email;

    class C04811 implements TextWatcher {
        C04811() {
        }

        public void afterTextChanged(Editable mEdit) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            LoginActivity.this.remember_me_checkbox.setChecked(false);
        }
    }

    class C04822 implements TextWatcher {
        C04822() {
        }

        public void afterTextChanged(Editable mEdit) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            LoginActivity.this.remember_me_checkbox.setChecked(false);
            if (LoginActivity.this.user_password_txt.getText().toString().startsWith(" ")) {
                CommonMethod.showAlert("Spaces not allowed", LoginActivity.this);
                LoginActivity.this.user_password_txt.setText("");
            }
        }
    }


    class C04833 implements View.OnFocusChangeListener {
        C04833() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
//            if (hasFocus) {
//                LoginActivity.this.rlBackgroundAdmission.setBackgroundResource(R.drawable.round_rectangle);
//            }
//            if (!hasFocus) {
//                LoginActivity.this.rlBackgroundAdmission.setBackgroundResource(R.drawable.roundgrey_rectangle);
//            }
        }
    }

    class C04844 implements View.OnFocusChangeListener {
        C04844() {
        }

        public void onFocusChange(View v, boolean hasFocus) {
//            if (hasFocus) {
//                Login.this.rlBackgroundPassword.setBackgroundResource(R.drawable.round_rectangle);
//            }
//            if (!hasFocus) {
//                Login.this.rlBackgroundPassword.setBackgroundResource(R.drawable.roundgrey_rectangle);
//            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext());
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        this.pDialog = new ProgressDialog(this);
        this.pDialog.setMessage("Loading...");
        getId();
        this.loginPreferences = getSharedPreferences("loginPrefs", 0);
        this.loginPrefsEditor = this.loginPreferences.edit();
        this.saveLogin = Boolean.valueOf(this.loginPreferences.getBoolean("saveLogin", false));
        if (this.saveLogin.booleanValue()) {
            this.user_name_txt.setText(this.loginPreferences.getString("user_name", ""));
            this.user_password_txt.setText(this.loginPreferences.getString("password", ""));
            this.remember_me_checkbox.setChecked(true);
        }

        this.user_name_txt.addTextChangedListener(new C04811());
        this.user_password_txt.addTextChangedListener(new C04822());
        this.user_name_txt.setOnFocusChangeListener(new C04833());
        this.user_password_txt.setOnFocusChangeListener(new C04844());
        FacebookLogin();
        googleLogin();
        method();
    }

    private void getId() {
        user_name_txt = findViewById(R.id.user_name_txt);
        user_password_txt = findViewById(R.id.user_password_txt);
        forgot_password_txt = findViewById(R.id.forgot_password_txt);
        user_profile = findViewById(R.id.user_profile);
        facebook = findViewById(R.id.facebook);
        google = findViewById(R.id.google);
        twitter = findViewById(R.id.twitter);
        login_btn = findViewById(R.id.login_btn);
        signup_btn = findViewById(R.id.signup_btn);
        remember_me_checkbox = findViewById(R.id.remember_me_checkbox);
        Picasso.with(this).load(R.drawable.user_profile).transform(new CircleTransform()).into(user_profile);

        signup_btn.setOnClickListener(this);
        login_btn.setOnClickListener(this);
        facebook.setOnClickListener(this);
        google.setOnClickListener(this);
        remember_me_checkbox.setOnClickListener(this);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.remember_me_checkbox:
                ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.user_name_txt.getWindowToken(), 0);
                this.name   = this.user_name_txt.getText().toString();
                this.password = this.user_password_txt.getText().toString();
                if (this.remember_me_checkbox.isChecked()) {
                    this.loginPrefsEditor.putBoolean("saveLogin", true);
                    this.loginPrefsEditor.putString("user_name", this.name);
                    this.loginPrefsEditor.putString("password", this.password);
                    this.loginPrefsEditor.commit();
                } else {
                    this.loginPrefsEditor.clear();
                    this.loginPrefsEditor.commit();
                }
                doSomethingElse();
                return;

            case R.id.login_btn:

                if(checkvalidation()) {
                    DbHelper dbHelper = new DbHelper(this);
                    LoginResponseData loginResponseData = dbHelper.getUserDataByLoginId(user_name_txt.getText().toString());
                    if (loginResponseData != null) {
                        this. name =loginResponseData.getUserName();
                        this. email =loginResponseData.getEmailAddress();
                        this. password =user_password_txt.getText().toString();
                        String id_tikok="@"+loginResponseData.getUserPassword();
                        if (loginResponseData.getUserPassword().equals(password)) {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("USER_PREFS", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("email", email);
                            editor.putString("tiktok_id", id_tikok);
                            editor.putString("name", name);
                            editor.commit();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            CommonMethod.showAlert("Please enter correct password", LoginActivity.this);
                        }
                    } else {
                        CommonMethod.showAlert("User didn't exist", LoginActivity.this);
                    }
                }

                return;
            case R.id.signup_btn:
                Intent intent1 = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent1);
                return;
            case R.id.facebook:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email", "user_birthday"));
                FacebookLogin();
                return;
            case R.id.google:
                googleSignIn();
            default:
                return;
        }
    }
    private void doSomethingElse() {
    }

    private boolean checkvalidation() {
        if (this.user_name_txt.getText().toString().trim().length() == 0) {
            CommonMethod.showAlert("please enter user name", this);
            //    this.ivUserIcon.setImageResource(R.mipmap.active_user_icon);
            return false;
        } else if (this.user_password_txt.getText().toString().length() == 0) {
            CommonMethod.showAlert("please enter password", this);
            return false;
        }
        return true;
    }

    private class LoginAsync extends AsyncTask<String, Void, String> {
        private LoginAsync() {
        }

        protected String doInBackground(String... params) {
            //      Toast.makeText(Login.this, "okk", Toast.LENGTH_SHORT).show();
            LoginActivity.this.CallServiceCategory();
            return null;
        }

        protected void onPostExecute(String result) {
            hideProgressDialog();
            Gson gson = new Gson();
            try {
                LoginActivity.this.loginResponceParser = (LoginResponseParser) gson.fromJson(LoginActivity.this.mResponce, LoginResponseParser.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            if (LoginActivity.this.loginResponceParser == null) {
                CommonMethod.showAlert(LoginActivity.this.getString(R.string.networkError_Message), LoginActivity.this);
            } else if (LoginActivity.this.loginResponceParser.status.equals("200")) {
//
//                for (int i = 0; i < loginResponceParser.employeeDetail.size(); i++) {
//                    LoginActivity.this.startActivity(new Intent(
//                            LoginActivity.this, SignupActivity.class));
//                    LoginActivity.this.finish();
//                }
            } else {
                CommonMethod.showAlert(LoginActivity.this.loginResponceParser.responseMessage.toString().trim(), LoginActivity.this);
            }
        }

        protected void onPreExecute() {

            LoginActivity.this.showProgressDialog();
        }

        protected void onProgressUpdate(Void... values) {
            // hideProgressDialog();
        }
    }


    private void showProgressDialog() {
        this.pDialog.show();
    }

    private void hideProgressDialog() {
        if (this.pDialog.isShowing()) {
            this.pDialog.dismiss();
        }
    }

    public String CallServiceCategory() {
        Map<String, String> params = new HashMap();
        params.put("EmpId", this.user_name_txt.getText().toString().trim());
        params.put("password", this.user_password_txt.getText().toString().trim());
        System.out.println("Params" + params);
        try {
            HttpUtility.sendPostRequestForLogin(BASE_URL + LOGIN, params);
            String[] response = HttpUtility.readMultipleLinesRespone();
            System.out.println("responsesssss" + response);
            if (0 < response.length) {
                String line = response[0];
                this.mResponce = line;
                System.out.println("zxZAxASXaX " + this.mResponce);
                return line;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        HttpUtility.disconnect();
        return this.mResponce;
    }

    //login with facebook........................
    public void FacebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResults) {
                        final String facebookTokenId = loginResults.getAccessToken().getToken();
                        final String fbid = loginResults.getAccessToken().getUserId();
                        final String providerType = "facebook";
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResults.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {
                                        // TODO Auto-generated method stub
                                        Log.v("ActivityLogin",
                                                response.toString());
                                        String profilePicUrl = null;
                                        try {
                                            Log.v("ActivityLogin", object.toString());
                                            if (object.has("picture")) {
                                                profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                            }
                                            String fbemail = object.getString("email");
                                            String first_name = object.getString("first_name");
                                            String last_name = object.getString("last_name");
                                            String email = object.getString("email");
                                            String id = object.getString("id");
                                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                                            Log.d("hhs", fbemail + " " + first_name + " " + last_name + " " + email);
                                            Toast.makeText(LoginActivity.this, fbemail + " " + first_name + " " + last_name + " " + email, Toast.LENGTH_LONG).show();
                                            String fbId = fbid;
                                            String fbTokenId = facebookTokenId;
//                                            detail = "?userMobile=" + fbemail + "&userPassword=" + fbId + "&type=social" + "&userId=" + fbId;
//                                            userId = fbId;
//                                            url = Contants.SERVICE_BASE_URL + Contants.Login + detail;
//                                            Login(url);

                                        } catch (JSONException e) {
                                            // TODO Auto-generated catch block

                                            e.printStackTrace();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters
                                .putString(
                                        "fields",
                                        "picture.type(large),id,name,email,gender,birthday,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Log.e("dd", "facebook login canceled");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.e("dd", "facebook login failed error");
                    }
                });
    }

    // how to get a hash code in android
    public void method() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.vinay.tiktok", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("MY KEY HASH:", sign);
              //  Toast.makeText(getApplicationContext(), sign, Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
            }
        }
    }

    //login with google....................
    public void googleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(Contants.LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(Contants.LOG_TAG, "onAuthStateChanged:signed_out");
                }// ...
            }
        };
    }


    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(Contants.LOG_TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        String emailId = acct.getId();
                        String tokenId = acct.getIdToken();
                        String email = acct.getEmail();
                        String name = acct.getDisplayName();
                        String given_name = acct.getGivenName();
                        String photo = String.valueOf(acct.getPhotoUrl());
                        Log.d("", "okk" + email + emailId + tokenId);
                        Toast.makeText(LoginActivity.this, email + " " + emailId + " " + tokenId + name + " " + given_name + "  " + "  " + photo, Toast.LENGTH_SHORT).show();
//                        detail = "?userMobile=" + email + "&userPassword=" + emailId + "&type=social" + "&userId=" + emailId;
//                        url = Contants.SERVICE_BASE_URL + Contants.Login + detail;
//                        userId = emailId;
//                        Login(url);
                        Log.d(Contants.LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(Contants.LOG_TAG, "signInWithCredential", task.getException());
                           /* Toast.makeText(ActivityLogin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();*/
                        }
                    }
                });
    }

}
