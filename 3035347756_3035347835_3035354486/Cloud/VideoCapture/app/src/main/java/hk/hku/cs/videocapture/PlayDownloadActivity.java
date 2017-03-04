package hk.hku.cs.videocapture;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static hk.hku.cs.videocapture.R.id.filename;
import static hk.hku.cs.videocapture.R.id.seekbar;

public class PlayDownloadActivity extends AppCompatActivity {

    private String userName;
    private String fileName;
    private String fileType;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_download);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        fileName = bundle.getString("FILE_NAME");
        fileType = bundle.getString("FILE_TYPE");
        final String preLocalPath = Environment.getExternalStorageDirectory() + "/videoCapture/";

        RelativeLayout localVideo = (RelativeLayout) findViewById(R.id.activity_play_download);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.music_download);
        VideoView video = (VideoView) findViewById(R.id.downloadView);
        ImageView image = (ImageView) findViewById(R.id.downloadImage);


        if (fileType.equals("mp4") || fileType.equals("3gp")) {

            //移除图片view
            localVideo.removeView(image);
            localVideo.removeView(linearLayout);

            //播放影音
            video.setVideoPath(preLocalPath + fileName);
            MediaController ctlr = new MediaController(this);
            video.setMediaController(ctlr);
            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //Toast.makeText(PlayDownloadActivity.this, "Done", Toast.LENGTH_SHORT).show();
                }
            });
            video.requestFocus();
            video.start();
        }
        else if (fileType.equals("png") || fileType.equals("jpg")) {
            //移除视频播放器
            localVideo.removeView(video);
            localVideo.removeView(linearLayout);
            Bitmap bitmap = BitmapFactory.decodeFile(preLocalPath + fileName);
            image.setImageBitmap(bitmap);
        }
        else if (fileType.equals("mp3")) {
            try {
                localVideo.removeView(video);
                localVideo.removeView(image);
                //设置拖动条
                seekBar = (SeekBar) findViewById(R.id.seekbar_download);
                ImageView imageView = (ImageView) findViewById(R.id.play_button_download);
                mediaPlayer = new MediaPlayer();
                File file = new File(preLocalPath+fileName);
                if(!file.exists()){
                    file.createNewFile();
                }
                FileInputStream fis = new FileInputStream(file);
                mediaPlayer.setDataSource(fis.getFD());
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
        }
        else {
            //移除视频图片view
            localVideo.removeView(video);
            localVideo.removeView(image);
            localVideo.removeView(linearLayout);

            //添加文字view
            TextView textView = new TextView(PlayDownloadActivity.this);
            textView.setText("Can not open this file");
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textView.setLayoutParams(layoutParams);
            localVideo.addView(textView);
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
