package hk.hku.cs.videocapture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.util.Timer;
import java.util.TimerTask;

import static hk.hku.cs.videocapture.R.id.seekbar;

public class PlayVideoActivity extends AppCompatActivity {

    private String userName;
    private String fileName;
    private String fileType;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private int JSON_SUCCESS = 100;
    private int BITMAP_SUCCESS = 10;
    private int DELETE_SUCCESS = 200;
    private Handler handler;
    private Bitmap bitmap;
    private VideoView video;
    private MediaController ctlr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_vedio);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        userName = bundle.getString("USER_NAME");
        fileName = bundle.getString("FILE_NAME");
        fileType = bundle.getString("FILE_TYPE");
        String newfileName = fileName.replace(" ", "%20");
        String pre = "http://i.cs.hku.hk/~zjzeng/upload/";
        final String path = pre + userName + "/" + newfileName;

        RelativeLayout playVideo = (RelativeLayout) findViewById(R.id.play);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.music);
        LinearLayout newLinearLayout = (LinearLayout) findViewById(R.id.cannot);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == BITMAP_SUCCESS) {
                    ImageView imageView = (ImageView) findViewById(R.id.play_image);
                    imageView.setImageBitmap(bitmap);
                }
                else{
                    Toast.makeText(PlayVideoActivity.this, "Download error!", Toast.LENGTH_SHORT).show();
                }
            }
        };


        if (fileType.equals("mp4") || fileType.equals("3gp")) {

            //播放影音
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            video = new VideoView(PlayVideoActivity.this);
            video.setVideoPath(path);
            ctlr = new MediaController(this);
            video.setMediaController(ctlr);
            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //Toast.makeText(PlayVideoActivity.this, "Done", Toast.LENGTH_SHORT).show();
                }
            });
            video.setLayoutParams(layoutParams);
            playVideo.addView(video);
            playVideo.removeView(linearLayout);
            playVideo.removeView(newLinearLayout);
            video.requestFocus();
            video.start();
        }
        else if (fileType.equals("mp3")) {
            try {
                //设置拖动条
                playVideo.removeView(newLinearLayout);
                seekBar = (SeekBar) findViewById(seekbar);
                ImageView imageView = (ImageView) findViewById(R.id.play_button);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(path);
                final Timer timer = new Timer();

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                        }
                        else{
                            mediaPlayer.start();
                        }
                    }
                });

                //设置监听器
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(final MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        seekBar.setMax(mediaPlayer.getDuration());

                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (mediaPlayer == null)
                                    return;
                                if (mediaPlayer.isPlaying() && seekBar.isPressed() == false) {
                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                }
                            }
                        };

                        //prepared后获取duration
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                mediaPlayer.seekTo(seekBar.getProgress());
                            }
                        });

                        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                            @Override
                            public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
                                seekBar.setSecondaryProgress(percent);
                                //Log.e(currentProgress + "% play", percent + " buffer");
                            }
                        });

                        timer.schedule(timerTask, 0,1000);
                    }

                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                    }
                });
                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (fileType.equals("png") || fileType.equals("jpg")) {
            ImageView newImage = new ImageView(PlayVideoActivity.this);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            newImage.setLayoutParams(layoutParams);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(path);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                        if (urlConnection.getResponseCode() == 200) {
                            BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                            bitmap = BitmapFactory.decodeStream(inputStream);
                            Message msg = new Message();
                            msg.what = BITMAP_SUCCESS;
                            handler.sendMessage(msg);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(runnable).start();
            newImage.setId(R.id.play_image);
            playVideo.addView(newImage);
            playVideo.removeView(linearLayout);
            playVideo.removeView(newLinearLayout);

        }
        else {
            playVideo.removeView(linearLayout);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==event.KEYCODE_BACK){
            if(mediaPlayer!=null){
                mediaPlayer.stop();
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
