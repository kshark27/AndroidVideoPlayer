package com.example.user.exoplayer.player.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.exoplayer.R;
import com.example.user.exoplayer.player.data.VideoSource;
import com.example.user.exoplayer.player.data.database.Subtitle;
import com.example.user.exoplayer.player.util.PlayerUiController;
import com.example.user.exoplayer.player.util.SubtitleAdapter;
import com.example.user.exoplayer.player.util.VideoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, PlayerUiController {

    private static final String TAG = "PlayerActivity";
    private PlayerView playerView;
    private VideoPlayer player;
    private ImageButton mute, unMute, subtitle, setting, lock, unLock, nextBtn;
    private ProgressBar progressBar;
    private AlertDialog alertDialog;
    private VideoSource videoSource;
    private AudioManager mAudioManager;
    private TextView title, cancel, noSub;
    private Typeface typeface;
    private boolean disableBackpress = false;

    /***********************************************************
     Handle audio on different events
     ***********************************************************/
    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (player != null)
                                //  player.getPlayer().setPlayWhenReady(true);
                                break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                            if (player != null)
                                player.getPlayer().setPlayWhenReady(false);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // Lost audio focus, but will gain it back (shortly), so note whether
                            // playback should resume
                            if (player != null)
                                player.getPlayer().setPlayWhenReady(false);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            // Lost audio focus, probably "permanently"
                            if (player != null)
                                player.getPlayer().setPlayWhenReady(false);
                            break;
                    }
                }
            };


    /***********************************************************
     Activity lifecycle
     ***********************************************************/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        getDataFromIntent();
        setupLayout();
        initSource();
    }

    private void getDataFromIntent() {
        videoSource = getIntent().getParcelableExtra("videoSource");
//        for (int i = 0; i < videoSource.getVideos().size(); i++) {
//            Log.d(TAG, "getDataFromIntent: " + videoSource.getVideos().get(i).getUrl());
//        }
    }

    private void setupLayout() {

        playerView = findViewById(R.id.demo_player_view);
        progressBar = findViewById(R.id.progress_bar);

        mute = findViewById(R.id.btn_mute);
        unMute = findViewById(R.id.btn_unMute);
        subtitle = findViewById(R.id.btn_subtitle);
        setting = findViewById(R.id.btn_settings);
        lock = findViewById(R.id.btn_lock);
        unLock = findViewById(R.id.btn_unLock);
        nextBtn = findViewById(R.id.exo_next);

        //optional setting
        playerView.getSubtitleView().setVisibility(View.GONE);

        mute.setOnClickListener(this);
        unMute.setOnClickListener(this);
        subtitle.setOnClickListener(this);
        setting.setOnClickListener(this);
        lock.setOnClickListener(this);
        unLock.setOnClickListener(this);
    }

    private void initSource() {

        if (videoSource.getVideos() == null) {
            Toast.makeText(this, "can not play video", Toast.LENGTH_SHORT).show();
            return;
        }

        player = new VideoPlayer(playerView, getApplicationContext(), videoSource, this);
        player.seekToOnDoubleTap();
        this.mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        player.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_ENDED:
                        nextBtn.performClick();
                        break;
                    case Player.STATE_READY:
                        Log.d(TAG, "onPlayerStateChanged: STATE_READY");
                        mAudioManager.requestAudioFocus(
                                mOnAudioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);
                        break;
                }
            }

        });

//        if (player.getCurrentVideo().getSubtitles() == null ||
//                player.getCurrentVideo().getSubtitles().size() == 0)
//
//            subtitle.setImageResource(R.drawable.exo_no_subtitle_btn);

        //optional setting
        playerView.getSubtitleView().setVisibility(View.GONE);
        player.seekToOnDoubleTap();

    }

    @Override
    public void onStart() {
        super.onStart();
        hideSystemUi();
        player.resumePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        player.resumePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        player.pausePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.releasePlayer();
    }

    @Override
    public void onBackPressed() {
        if (disableBackpress)
            return;

        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUi();
    }

    @Override
    public void onClick(View view) {
        int controllerId = view.getId();

        switch (controllerId) {
            case R.id.btn_mute:
                player.setMute(true);
                break;
            case R.id.btn_unMute:
                player.setMute(false);
                break;
            case R.id.btn_settings:
                player.setSelectedQuality(this);
                break;
            case R.id.btn_subtitle:
                prepareSubtitles();
                break;
            case R.id.btn_lock:
                updateLockMode(true);
                break;
            case R.id.btn_unLock:
                updateLockMode(false);
            case R.id.exo_rew:
                player.seekToSelectedPosition(0, true);
                break;
            default:
                break;
        }


        if (controllerId == R.id.btn_lock) {
            updateLockMode(true);
        }

        if (controllerId == R.id.btn_unLock) {
            updateLockMode(false);
        }

    }

    /***********************************************************
     UI config
     ***********************************************************/
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void showSubtitle(boolean show) {

        if (player == null || playerView.getSubtitleView() == null)
            return;

        if (!show) {
            playerView.getSubtitleView().setVisibility(View.GONE);
            return;
        }

        alertDialog.dismiss();
        playerView.getSubtitleView().setVisibility(View.VISIBLE);
    }

    @Override
    public void changeSubtitleBackground() {
        CaptionStyleCompat captionStyleCompat = new CaptionStyleCompat(Color.YELLOW, Color.TRANSPARENT, Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.LTGRAY, null);
        playerView.getSubtitleView().setStyle(captionStyleCompat);
    }

    private void prepareSubtitles() {
        if (player == null || playerView.getSubtitleView() == null)
            return;

        if (player.getCurrentVideo().getSubtitles() == null ||
                player.getCurrentVideo().getSubtitles().size() == 0) {
            Toast.makeText(this, "زیرنویس موجود نیست.", Toast.LENGTH_SHORT).show();
            return;
        }

        player.pausePlayer();
        showSubtitleDialog();

    }

    private void showSubtitleDialog() {
        //init subtitle dialog
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);


        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.subtitle_selection_dialog, null);

        title = view.findViewById(R.id.subtitle_dialog_header);
        cancel = view.findViewById(R.id.cancel_dialog_btn);
        noSub = view.findViewById(R.id.no_subtitle_text_view);

        builder.setView(view);
        alertDialog = builder.create();


        // set the height and width of dialog
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;

        alertDialog.getWindow().setAttributes(layoutParams);

        RecyclerView recyclerView = view.findViewById(R.id.subtitle_recycler_view);
        recyclerView.setAdapter(new SubtitleAdapter(player.getCurrentVideo().getSubtitles(), player));

        TextView noSubtitle = view.findViewById(R.id.no_subtitle_text_view);
        noSubtitle.setOnClickListener(view1 -> {
            if (playerView.getSubtitleView().getVisibility() == View.VISIBLE)
                showSubtitle(false);
            alertDialog.dismiss();
            player.resumePlayer();
        });

        Button cancelDialog = view.findViewById(R.id.cancel_dialog_btn);
        cancelDialog.setOnClickListener(view1 -> {
            alertDialog.dismiss();
            player.resumePlayer();
        });

        // to prevent dialog box from getting dismissed on outside touch
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void setMuteMode(boolean mute) {
        if (player != null && playerView != null) {
            if (mute) {
                this.mute.setVisibility(View.GONE);
                unMute.setVisibility(View.VISIBLE);
            } else {
                unMute.setVisibility(View.GONE);
                this.mute.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateLockMode(boolean isLock) {
        if (player == null || playerView == null)
            return;

        player.lockScreen(isLock);

        if (isLock) {
            disableBackpress = true;
            playerView.hideController();
            unLock.setVisibility(View.VISIBLE);
            return;
        }

        disableBackpress = false;
        playerView.showController();
        unLock.setVisibility(View.GONE);

    }

    @Override
    public void showProgressBar(boolean visible) {
        if (visible)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);
    }


}