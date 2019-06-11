package com.gt05.lanisya.Breakoutgame;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "MainActivity";

    BreakoutView breakoutView;
    private AdView mAdView;

    private GoogleSignInClient googleSignInClient = null;
    final public int BUTTON_SIGN_IN = 2;
    final public int BUTTON_SIGN_OUT = 1;

    Button signOutBtn;
    SignInButton signInButton;

    boolean isAuth = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        breakoutView = new BreakoutView(this);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        FrameLayout game = new FrameLayout(this);
        LinearLayout gameWidgets = new LinearLayout(this);

        Button endGameButton = new Button(this);

        endGameButton.setWidth(300);
        endGameButton.setText("Start Game");

        //Banner
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        RelativeLayout relativeLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams adViewParams = new RelativeLayout.LayoutParams(
                AdView.LayoutParams.WRAP_CONTENT,
                AdView.LayoutParams.WRAP_CONTENT);
        // align bottom
        adViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        // align center
        adViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        relativeLayout.addView(adView, adViewParams);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).requestEmail().build();

        signOutBtn = new Button(this);
        signOutBtn.setId(BUTTON_SIGN_OUT);
        signOutBtn.setWidth(300);
        signOutBtn.setText("Sign Out");
        signOutBtn.setOnClickListener(this);

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        signInButton = new SignInButton(this);
        signInButton.setId(BUTTON_SIGN_IN);
        signInButton.setOnClickListener(this);
        signInButton.setSize(signInButton.SIZE_STANDARD);


        gameWidgets.addView(signInButton);
        gameWidgets.addView(signOutBtn);

        if(!isAuth){
            signInButton.setVisibility(View.GONE);
        }else{
            signInButton.setVisibility(View.GONE);
        }
        adView.loadAd(adRequest);

        game.addView(breakoutView);
        game.addView(relativeLayout);
        game.addView(gameWidgets);

        setContentView(game);
    }


    public void onClick(View view){
        switch (view.getId()){
            case BUTTON_SIGN_IN:
                Log.d(TAG, "BUTTON_SIGN_IN");
                Toast.makeText(this,"SIGN IN",Toast.LENGTH_SHORT).show();
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivity(signInIntent);

                signOutBtn.setVisibility(View.VISIBLE);
                signInButton.setVisibility(View.GONE);
                break;

            case BUTTON_SIGN_OUT:
                Toast.makeText(this,"sign out", Toast.LENGTH_SHORT).show();
                googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        signOutBtn.setVisibility(View.GONE);
                        signInButton.setVisibility(View.VISIBLE);
                    }
                });
                break;
                default:
                    break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        breakoutView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        breakoutView.pause();
    }
}
