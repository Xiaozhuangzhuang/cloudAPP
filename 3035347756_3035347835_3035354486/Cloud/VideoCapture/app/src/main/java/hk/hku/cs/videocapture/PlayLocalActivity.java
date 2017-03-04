package hk.hku.cs.videocapture;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlayLocalActivity extends AppCompatActivity {

    private String userName;
    private String fileName;
    private String fileType;
    private Handler handler;
    private final int JSON_SUCCESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_local);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        userName = bundle.getString("USER_NAME");
        fileName = bundle.getString("FILE_NAME");
        fileType = bundle.getString("FILE_TYPE");
        final String preLocalPath = Environment.getExternalStorageDirectory() + "/saveCapture/";

        Button button = (Button) findViewById(R.id.upload_local);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload(userName,fileType,preLocalPath+fileName);
            }
        });

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == JSON_SUCCESS) {
                    Toast.makeText(PlayLocalActivity.this, "Upload sucess!", Toast.LENGTH_SHORT).show();
                }
                {
                    Toast.makeText(PlayLocalActivity.this, "Uplaod error!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (fileType.equals("mp4") || fileType.equals("3gp")) {

            //移除图片view
            LinearLayout localVideo = (LinearLayout) findViewById(R.id.local);
            VideoView video = (VideoView) findViewById(R.id.localView);
            ImageView image = (ImageView) findViewById(R.id.localImage);
            localVideo.removeView(image);

            //播放影音
            video.setVideoPath(preLocalPath + fileName);
            MediaController ctlr = new MediaController(this);
            video.setMediaController(ctlr);
            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(PlayLocalActivity.this, "Done", Toast.LENGTH_SHORT).show();
                }
            });
            video.requestFocus();
            video.start();
        }
        else if (fileType == "png" || fileType == "jpg") {
            //移除视频播放器
            LinearLayout localVideo = (LinearLayout) findViewById(R.id.local);
            VideoView video = (VideoView) findViewById(R.id.localView);
            ImageView image = (ImageView) findViewById(R.id.localImage);
            localVideo.removeView(video);
            Bitmap bitmap = BitmapFactory.decodeFile(preLocalPath + fileName);
            image.setImageBitmap(bitmap);
        }
        else {
            //移除视频图片view
            LinearLayout localVideo = (LinearLayout) findViewById(R.id.local);
            VideoView video = (VideoView) findViewById(R.id.localView);
            ImageView image = (ImageView) findViewById(R.id.localImage);
            localVideo.removeView(video);
            localVideo.removeView(image);

            //添加文字view
            TextView textView = new TextView(PlayLocalActivity.this);
            textView.setText("Can not open this file");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            textView.setLayoutParams(layoutParams);
            localVideo.addView(textView);
        }
    }

    protected void upload(final String username,final String filetype,final String srcPath){
        final ProgressDialog pdialog = new ProgressDialog(this);
        pdialog.setCancelable(false);
        pdialog.setMessage("uploading ...");
        pdialog.show();
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            String check_result = null;
            String upload_result = null;
            @Override
            protected String doInBackground(String... arg0) {

                String filename = srcPath.substring(srcPath.lastIndexOf("/") + 1);
                try {
                    String url = "http://i.cs.hku.hk/~zjzeng/checkreapt.php?username="+username+"&filename="+filename;
                    URL url_cshomepage = new URL(url);
                    HttpURLConnection conn_cshomepage = (HttpURLConnection) url_cshomepage.openConnection();
                    conn_cshomepage.setDoInput(true);
                    conn_cshomepage.setUseCaches(false);
                    conn_cshomepage.setRequestMethod("GET");
                    conn_cshomepage.setRequestProperty("Connection", "Keep-Alive");
                    conn_cshomepage.setRequestProperty("Charset", "UTF-8");

                    int responseCode = conn_cshomepage.getResponseCode();
                    if (responseCode == 200) {
                        //获取连接的输入流，这个输入流就是文件的输入流
                        InputStream is = conn_cshomepage.getInputStream();
                        int ch;
                        StringBuffer b = new StringBuffer();
                        while ((ch = is.read()) != -1) {
                            b.append((char) ch);
                        }
                        check_result = b.toString();
                    }
                    conn_cshomepage.disconnect();

                } catch (Exception e)
                {
                    e.printStackTrace();
                    setTitle(e.getMessage());
                }
                if (check_result.equals("success.")){
                    try {
                        String end = "\r\n";
                        String twoHyphens = "--";
                        String boundary = "******";
                        String url = "http://i.cs.hku.hk/~zjzeng/upload.php";
                        URL url_cshomepage = new URL(url);
                        HttpURLConnection conn_cshomepage = (HttpURLConnection) url_cshomepage.openConnection();
                        conn_cshomepage.setDoInput(true);
                        conn_cshomepage.setDoOutput(true);
                        conn_cshomepage.setUseCaches(false);
                        conn_cshomepage.setRequestMethod("POST");
                        conn_cshomepage.setRequestProperty("Connection", "Keep-Alive");
                        conn_cshomepage.setRequestProperty("Charset", "UTF-8");
                        conn_cshomepage.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);

                        DataOutputStream ds = new DataOutputStream(
                                conn_cshomepage.getOutputStream());
                        ds.writeBytes(twoHyphens + boundary + end);
                        filename=filetype+"_"+username+"_"+srcPath.substring(srcPath.lastIndexOf("/") + 1) ;
                        ds.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+filename+"\""+end);
                        ds.writeBytes(end);

                        FileInputStream fis = new FileInputStream(srcPath);
                        byte[] buffer = new byte[8192]; // 8k
                        int count = 0;
                        // 读取文件
                        while ((count = fis.read(buffer)) != -1)
                        {
                            ds.write(buffer, 0, count);
                        }

                        ds.writeBytes(end);
                        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
                        ds.flush();
                        ds.close();
                        fis.close();
                        int responseCode = conn_cshomepage.getResponseCode();
                        if (responseCode == 200) {
                            //获取连接的输入流，这个输入流就是图片的输入流
                            InputStream is = conn_cshomepage.getInputStream();
                            int ch;
                            StringBuffer b = new StringBuffer();
                            while ((ch = is.read()) != -1) {
                                b.append((char) ch);
                            }
                            upload_result = b.toString();
                        }
                        conn_cshomepage.disconnect();
                    }  catch (Exception e)
                    {
                        e.printStackTrace();
                        setTitle(e.getMessage());
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(String result) {
                if (check_result.equals("success.")) {
                    Toast.makeText(getApplicationContext(),upload_result,Toast.LENGTH_LONG).show();
                    //上传结束后返回显示界面
                } else if(check_result.equals("name conflict.")) {
                    alert( "Warning", "File is included in the server,\nPlease change a name." );
                }
                pdialog.hide();
            }
        }.execute("");
    }
    protected void alert(String title, String mymessage){
        new AlertDialog.Builder(this)
                .setMessage(mymessage)
                .setTitle(title)
                .setCancelable(true)
                .setNegativeButton("confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton){}
                        }
                )
                .show();
    }
}
