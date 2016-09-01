package com.lue.live.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lue.live.R;
import com.panframe.android.lib.PFAsset;
import com.panframe.android.lib.PFAssetObserver;
import com.panframe.android.lib.PFAssetStatus;
import com.panframe.android.lib.PFNavigationMode;
import com.panframe.android.lib.PFObjectFactory;
import com.panframe.android.lib.PFView;

import java.util.Timer;
import java.util.TimerTask;

public class PanframePlayActivity extends AppCompatActivity implements PFAssetObserver, SeekBar.OnSeekBarChangeListener
        , View.OnClickListener
{

    //Panframe SDK
    private PFView pfView;
    private PFAsset pfAsset;
    private PFNavigationMode currentmode = PFNavigationMode.MOTION;
    private boolean VR_Mode = false;
    private boolean updateThumb = true;
    private Timer seekbarTimer;
    private int playing_mode;


    //UI相关
    private ViewGroup frameContainer;
    private RelativeLayout rl_bottom;
    private ImageButton backButton;
    private ImageButton stopButton;
    private ImageButton playButton;
    private ImageButton vrButton;
    private ImageButton touchButton;
    private SeekBar seekBar_video;
    private RelativeLayout rl_top;
    private TextView tv_title;
    private ImageButton settingsButton;
    private View videoView1 = null;
    private GestureDetectorCompat mDetector;

    //与上下两行菜单相关
    private boolean is_Menus_Shown = true;
    private float rl_top_height;
    private float rl_bottom_height;
    private float screen_width, screen_height;
    private WindowManager windowManager;

    //计时器
    private Timer mTimer;
    private TouchTimerTask mTimerTask1;
    private TouchTimerTask mTimerTask2;

    //视频信息
    private String file_path;
    private String title;

    //设置菜单相关
    private PopupWindow popupWindow_setting;
    private SeekBar seekBar_lighting;
    private SeekBar seekBar_voice;
    private RadioGroup radioGroup_videosize;
    private RadioGroup radioGroup_playingmode;


    //当前屏幕亮度的模式
    private int screenMode;
    //当前屏幕亮度值 0--255
    private int screenBrightness;
    private int maxVolume; // 最大音量值
    private int curVolume; // 当前音量值
    private AudioManager audioMgr = null; // Audio管理器，用了控制音量
    private float aspect_ratio;


    //处理动画的handler
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 1)
            {
                Show_Anim();
            }
            else
            {
                popupWindow_setting.dismiss();
                Show_Anim();
            }
        }
    };


    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panframeplay);

        Intent intent = getIntent();
        file_path = intent.getStringExtra("path");
        title = intent.getStringExtra("title");
        aspect_ratio = (float)intent.getDoubleExtra("ratio", 1);
        aspect_ratio = 1/aspect_ratio;
        playing_mode = intent.getIntExtra("playing_mode", 0);

        InitView();

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        loadVideo(file_path);
        pfAsset.play();

        //启动计时器
        mTimer = new Timer(true);
        StartTimerTask(1);

        init_settings();
    }

    /**
     * 初始化UI
     */
    public void InitView()
    {
        frameContainer = (ViewGroup) findViewById(R.id.framecontainer);
        frameContainer.setBackgroundColor(0xFF000000);
        frameContainer.setOnClickListener(this);
        frameContainer.setBackgroundColor(0xFF000000);

        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        playButton = (ImageButton) findViewById(R.id.playbutton);
        stopButton = (ImageButton) findViewById(R.id.stopbutton);
        vrButton = (ImageButton) findViewById(R.id.vrbutton);
        touchButton = (ImageButton) findViewById(R.id.touchbutton);
        seekBar_video = (SeekBar) findViewById(R.id.scrubber);

        rl_top = (RelativeLayout) findViewById(R.id.rl_top);
        backButton = (ImageButton) findViewById(R.id.play_back);
        tv_title = (TextView) findViewById(R.id.text_title);
        tv_title.setText(title);
        settingsButton = (ImageButton) findViewById(R.id.button_settings);

        playButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        vrButton.setOnClickListener(this);
        touchButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        seekBar_video.setOnSeekBarChangeListener(this);
        seekBar_video.setEnabled(false);

        rl_top_height = getResources().getDimension(R.dimen.vedio_controls_height);
        rl_bottom_height = rl_top_height;

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        screen_width = windowManager.getDefaultDisplay().getWidth();
        screen_height = windowManager.getDefaultDisplay().getHeight();
    }

    /**
     * 初始化设置菜单相关
     */
    private void init_settings()
    {
        try
        {
            /*
            * 获得当前屏幕亮度的模式
            * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
            * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
            */
            screenMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            // 获得当前屏幕亮度值 0--255
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);

            // 如果当前的屏幕亮度调节调节模式为自动调节，则改为手动调节屏幕亮度
            if (screenMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
            {
                setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        }
        catch (Settings.SettingNotFoundException e)
        {
            e.printStackTrace();
        }

        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取最大音乐音量
        maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
     */
    private void setScreenMode(int value)
    {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, value);
    }

    /**
     * 设置当前屏幕亮度值 0--255，并使之生效
     */
    private void setScreenBrightness(float value)
    {
        Window mWindow = getWindow();
        WindowManager.LayoutParams mParams = mWindow.getAttributes();
        float f = value / 255.0F;
        mParams.screenBrightness = f;
        mWindow.setAttributes(mParams);

        // 保存设置的屏幕亮度值
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) value);
    }


    /**
     * 展示上下两个菜单栏的动画
     */
    private void Show_Anim()
    {
        float y1, y2;
        ObjectAnimator top_anim, bottom_anim;
        if (is_Menus_Shown)
        {
            is_Menus_Shown = false;

            y1 = rl_top.getTranslationY();
            top_anim = ObjectAnimator.ofFloat(rl_top, "translationY", y1, 0 - rl_top_height);
            top_anim.setDuration(1000);
            top_anim.start();

            y2 = rl_bottom.getTranslationY();
            bottom_anim = ObjectAnimator.ofFloat(rl_bottom, "translationY", y2, rl_top_height);
            bottom_anim.setDuration(1000);
            bottom_anim.start();

            mTimerTask1.cancel();
        }
        else
        {
            is_Menus_Shown = true;

            y1 = rl_top.getTranslationY();
            top_anim = ObjectAnimator.ofFloat(rl_top, "translationY", y1, 0);
            top_anim.setDuration(1000);
            top_anim.start();

            y2 = rl_bottom.getTranslationY();
            bottom_anim = ObjectAnimator.ofFloat(rl_bottom, "translationY", y2, 0);
            bottom_anim.setDuration(1000);
            bottom_anim.start();

            StartTimerTask(1);
        }
    }


    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (pfView != null)
            pfView.handleOrientationChange();
    }


    /**
     * 为上下两个菜单栏设定计时器, flag是为了区分是设置PopupWindow还是上下两个菜单栏
     */
    public void StartTimerTask(int flag)
    {
        if (mTimer != null)
        {
            if (mTimerTask1 != null)
            {
                mTimerTask1.cancel();
            }
            if (mTimerTask2 != null)
            {
                mTimerTask2.cancel();
            }

            if(flag == 1)
            {
                mTimerTask1 = new TouchTimerTask(flag);
                mTimer.schedule(mTimerTask1, 3000);
            }
            else
            {
                mTimerTask2 = new TouchTimerTask(flag);
                mTimer.schedule(mTimerTask2, 3000);
            }
        }
    }


    /**
     * 计时器
     */
    class TouchTimerTask extends TimerTask
    {
        int flag;
        public TouchTimerTask(int flag)
        {
            this.flag = flag;
        }

        @Override
        public void run()
        {
            Message message = new Message();
            if(flag == 1)
                message.what = 1;
            else
                message.what = 2;
            handler.sendMessage(message);
        }
    }


    /**
     * Start the video with a local file path
     *
     * @param filename The file path on device storage
     */
    public void loadVideo(String filename)
    {

        pfView = PFObjectFactory.view(this);
        pfAsset = PFObjectFactory.assetFromUri(this, Uri.parse(filename), this);

        pfView.displayAsset(pfAsset);
        pfView.setNavigationMode(currentmode);
        pfView.setMode(playing_mode, aspect_ratio);

        //设置显示比例，先以高度充满为准，检测按照传入的比例的话宽度是否会超出。若超出，则以宽度充满为准，对高度进行调整。
        int target_width, target_height;
        target_height = (int)screen_height;
        target_width = (int)(target_height*aspect_ratio);
        if(target_width > screen_width)
        {
            target_width = (int)screen_width;
            target_height = (int)(target_width/aspect_ratio);
        }
        FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(target_width, target_height);
        lp1.gravity = Gravity.CENTER;
        frameContainer.setLayoutParams(lp1);

        videoView1 = pfView.getView();
        videoView1.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                mDetector.onTouchEvent(event);
                return false;
            }
        });
        frameContainer.addView(videoView1, 0);
    }

    /**
     * Status callback from the PFAsset instance.
     * Based on the status this function selects the appropriate action.
     *
     * @param asset  The asset who is calling the function
     * @param status The current status of the asset.
     */
    public void onStatusMessage(final PFAsset asset, PFAssetStatus status)
    {
        switch (status)
        {
            case LOADED:
                Log.d("LiveDemo", "Loaded");
                break;
            case DOWNLOADING:
                Log.d("LiveDemo", "Downloading 360 movie: " + pfAsset.getDownloadProgress() + " percent complete");
                break;
            case DOWNLOADED:
                Log.d("LiveDemo", "Downloaded to " + asset.getUrl());
                break;
            case DOWNLOADCANCELLED:
                Log.d("LiveDemo", "Download cancelled");
                break;
            case PLAYING:
                Log.d("LiveDemo", "Playing");
                playButton.setBackgroundResource(R.mipmap.pause);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                seekBar_video.setEnabled(true);
                seekBar_video.setMax((int) asset.getDuration());
                if (seekbarTimer == null)
                {
                    seekbarTimer = new Timer();
                    final TimerTask task = new TimerTask()
                    {
                        public void run()
                        {
                            if (updateThumb)
                            {
                                seekBar_video.setMax((int) asset.getDuration());
                                seekBar_video.setProgress((int) asset.getPlaybackTime());
                            }
                        }
                    };
                    seekbarTimer.schedule(task, 0, 33);
                }
                break;
            case PAUSED:
                Log.d("LiveDemo", "Paused");
                playButton.setBackgroundResource(R.mipmap.play);
                break;
            case STOPPED:
                Log.d("LiveDemo", "Stopped");
                playButton.setBackgroundResource(R.mipmap.play);
                if (seekbarTimer != null)
                {
                    seekbarTimer.cancel();
                    seekbarTimer.purge();
                    seekbarTimer = null;
                }
                seekBar_video.setProgress(0);
                seekBar_video.setEnabled(false);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            case COMPLETE:
                Log.d("LiveDemo", "Complete");
                playButton.setBackgroundResource(R.mipmap.play);
                if (seekbarTimer != null)
                {
                    seekbarTimer.cancel();
                    seekbarTimer.purge();
                    seekbarTimer = null;
                }
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            case ERROR:
                Log.d("LiveDemo", "Error");
                break;
        }
    }

    /**
     * Called when pausing the app.
     * This function pauses the playback of the asset when it is playing.
     */
    public void onPause()
    {
        super.onPause();
        if (pfAsset != null)
        {
            if (pfAsset.getStatus() == PFAssetStatus.PLAYING)
                pfAsset.pause();
        }
    }


    //实现的SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser)
    {
    }

    ////实现的SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekbar)
    {
        updateThumb = false;
    }

    ////实现的SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekbar)
    {
        int id = seekbar.getId();

        switch (id)
        {
            case R.id.scrubber:
                pfAsset.setPLaybackTime(seekbar.getProgress());
                updateThumb = true;
                break;
            case R.id.seekbar_light:
                setScreenBrightness(seekbar.getProgress());
                StartTimerTask(2);
                break;
            case R.id.seekbar_voice:
                curVolume = seekbar.getProgress();
                audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume,
                        AudioManager.FLAG_PLAY_SOUND);
                StartTimerTask(2);
                break;
            default:
                break;
        }

    }


    //实现的View.OnClickListener
    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.play_back:
                finish();
                break;
            case R.id.stopbutton:
                if (pfAsset == null)
                    return;

                pfAsset.stop();

                // cleanup
                pfView.release();
                pfAsset.release();

                pfAsset = null;

                frameContainer.removeView(videoView1);
                pfView = null;
                break;
            case R.id.playbutton:
                if (pfAsset == null)
                    loadVideo(file_path);

                if (pfAsset.getStatus() == PFAssetStatus.PLAYING)
                {
                    pfAsset.pause();
                }
                else
                {
                    if (pfView != null)
                    {
                        pfView.injectImage(null);
                    }
                    pfAsset.play();
                }
                break;
            case R.id.vrbutton:
                if (pfView != null)
                {
                    if (VR_Mode)
                    {
                        //返回默认效果
                        vrButton.setBackgroundResource(R.mipmap.vr);
                        pfView.setMode(0, 0);
                        VR_Mode = false;
                    }
                    else
                    {
                        //设置VR模式，即并排显示两个视频
                        vrButton.setBackgroundResource(R.mipmap.panoramic);
                        pfView.setMode(2, 0);
                        VR_Mode = true;
                    }
                }
                break;
            case R.id.touchbutton:
                if (pfView != null)
                {
                    if (currentmode == PFNavigationMode.TOUCH)
                    {
                        currentmode = PFNavigationMode.MOTION;
                        touchButton.setBackgroundResource(R.mipmap.gyroscope);
                    }
                    else
                    {
                        currentmode = PFNavigationMode.TOUCH;
                        touchButton.setBackgroundResource(R.mipmap.touch);
                    }
                    pfView.setNavigationMode(currentmode);
                }
                break;

            case R.id.framecontainer:
                Show_Anim();
                break;
            case R.id.button_settings:
                if (popupWindow_setting == null)
                {
                    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupview = layoutInflater.inflate(R.layout.layout_setting_popupwindow, null);

                    seekBar_lighting = (SeekBar) popupview.findViewById(R.id.seekbar_light);
                    seekBar_voice = (SeekBar) popupview.findViewById(R.id.seekbar_voice);
                    radioGroup_videosize = (RadioGroup)popupview.findViewById(R.id.radio_video_size);
                    radioGroup_playingmode = (RadioGroup)popupview.findViewById(R.id.radio_playing_mode);

                    seekBar_lighting.setProgress(screenBrightness);
                    seekBar_lighting.setOnSeekBarChangeListener(this);
                    seekBar_voice.setOnSeekBarChangeListener(this);
                    seekBar_voice.setMax(maxVolume);
                    seekBar_voice.setProgress(curVolume);

                    radioGroup_videosize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                    {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId)
                        {
                            switch (checkedId)
                            {
                                case R.id.sixteen_nine:
                                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT);
                                    frameContainer.setLayoutParams(lp);
                                    break;
                                case R.id.four_three:
                                    FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams((int)(screen_height*4)/3, (int)screen_height);
                                    lp1.gravity = Gravity.CENTER;
                                    frameContainer.setLayoutParams(lp1);
                                    break;
                                default:
                                    break;
                            }
                            StartTimerTask(2);
                        }
                    });

                    radioGroup_playingmode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                    {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId)
                        {
                            switch (checkedId)
                            {
                                case R.id.mode_0:
                                    pfView.setMode(0, aspect_ratio);
                                    break;
                                case R.id.mode_1:
                                    pfView.setMode(1, aspect_ratio);
                                    break;
                                case R.id.mode_2:
                                    pfView.setMode(2, aspect_ratio);
                                    break;
                                default:
                                    break;
                            }
                            StartTimerTask(2);
                        }
                    });

                    popupWindow_setting = new PopupWindow(popupview, LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    popupWindow_setting.setFocusable(true);
                    popupWindow_setting.setOutsideTouchable(true);
                    popupWindow_setting.setBackgroundDrawable(new BitmapDrawable());
                }

                if (!popupWindow_setting.isShowing())
                {
                    popupWindow_setting.showAsDropDown(settingsButton,0,
                            (int)getResources().getDimension(R.dimen.playactivity_menu_demen));
                    StartTimerTask(2);
                }
                else
                    popupWindow_setting.dismiss();

                break;

            default:
                break;
        }
    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            Show_Anim();
            return super.onSingleTapConfirmed(e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    @Override
    public void onBackPressed()
    {
        finish();
    }
}
