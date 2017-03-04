package hk.hku.cs.videocapture;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by HP on 2016/11/24.
 */

public class VideoCaptureActivity extends AppCompatActivity {
    private String context;
    private static final int VIDEO_CAPTURE_REQUEST = 1111;
    private static final int VIDEO_CAPTURE_PERMISSION = 2222;
    private static final int UPLOAD_SUCCESSED = 1;
    private VideoView mVideoView;
    private String fileName;
    private File temp;
    private String userName;
    private String fileType;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_fragment);
        userName = getIntent().getStringExtra("USER_NAME");
        fileType = "video";

        handler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == UPLOAD_SUCCESSED){
                    Toast toast = Toast.makeText(VideoCaptureActivity.this,"Upload Success!",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    //结束上一个界面
                    if(CloudActivity.instance!=null)
                        CloudActivity.instance.finish();
                    Intent intent = new Intent(VideoCaptureActivity.this,CloudActivity.class);
                    intent.putExtra("USER_NAME",userName);
                    intent.putExtra("FILE_TYPE","all");
                    intent.putExtra("ORDER","filename");
                    startActivity(intent);
                    finish();
                    //切换当前fragment至local视频界面
                }
            }
        };
        Button uButton = (Button) findViewById(R.id.upload);
        uButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveVideo(1);
            }
        });
        Button sButton = (Button) findViewById(R.id.save);
        sButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveVideo(0);
                //切换当前fragment至local视频界面

            }
        });


        //Log.d(TAG, "************************************** enter create...");
        mVideoView = (VideoView) findViewById(R.id.video_image);

        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(VideoCaptureActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(VideoCaptureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(VideoCaptureActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(VideoCaptureActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }

        if(permissions.size() > 0) {
            String[] permiss = permissions.toArray(new String[0]);

            ActivityCompat.requestPermissions(VideoCaptureActivity.this, permiss,
                    VIDEO_CAPTURE_PERMISSION);
        } else {
            StartVideoCapture();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIDEO_CAPTURE_REQUEST && resultCode == Activity.RESULT_OK) {

            Uri videoUri = data.getData();

            MediaController mediaController= new MediaController(VideoCaptureActivity.this);
            mediaController.setAnchorView(mVideoView);

            mVideoView.setMediaController(mediaController);
            mVideoView.setVideoURI(videoUri);
            mVideoView.requestFocus();

            mVideoView.start();
        }
        else{
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VIDEO_CAPTURE_PERMISSION) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                StartVideoCapture();
            }
            else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }

    private void StartVideoCapture() {
        Uri viduri = getOutputMediaFileUri();

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, viduri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (4 * 1024 * 1024));

        this.startActivityForResult(intent, VIDEO_CAPTURE_REQUEST);
    }

    private Uri getOutputMediaFileUri() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        if (isExternalStorageAvailable()) {
            // get the Uri

            //1. Get the external storage directory
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath()+"/saveCapture");

            //2. Create our subdirectory
            if (! mediaStorageDir.exists()) {
                if(! mediaStorageDir.mkdirs()){
                    //Log.e(TAG, "Failed to create directory.");
                    return null;
                }
            }
            //3. Create a file name
            //4. Create the file
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
            fileName = "VID_"+timestamp;//为filename赋值

            String path = mediaStorageDir.getPath() + File.separator;

            temp = new File(path + "VID_" + timestamp + ".mp4");

            //Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            //5. Return the file's URI
            return Uri.fromFile(temp);
        } else {
            return null;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        } else {
            return false;
        }
    }

    /*public void uploadVideos(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    //变量定义及初始化
                    String pre = "http://i.cs.hku.hk/~zjzeng/php/upload.php";
                    String end = "\r\n";//流的最后需要加上回车
                    String boundary = "******";//分隔符，格式要求，可以为任何字符串
                    File destDir = new File(Environment.getExternalStorageDirectory()+"/videoCapture/");

                    //建立httpURLConnection连接并进行设置
                    URL url = new URL(pre);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    //设置http头的分割符形式以及传输内容类型
                    urlConnection.setRequestProperty("Content-Type","multipart/form-data; boundary="+boundary);

                    if (destDir.exists()) {
                        File file = new File(destDir+"/"+fileName+".mp4");
                        if(file.exists()){
                            //新建数据输出流和文件输入流
                            DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                            FileInputStream fileInputStream = new FileInputStream(file);

                            //模仿表单
                            outputStream.writeBytes("--" + boundary + end);
                            //(Content-Disposition: form-data; name="file";filename="filename"\r\n)
                            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+fileName+".mp4\""+end);
                            outputStream.writeBytes("Content-Type:audio/mp4"+end+end);

                            //读出文件流，写入输出流
                            byte[] by = new byte[1024];
                            int len = 0;;
                            while((len=fileInputStream.read(by))!=-1){
                                outputStream.write(by,0,len);
                            }
                            outputStream.writeBytes(end);
                            outputStream.writeBytes("--" + boundary + "--" + end);
                            //outputStream.writeBytes(boundary+"--"+end+"--"+boundary);//流末尾以回车换行结束

                            //结束后关闭所有流和断开连接
                            int a = urlConnection.getResponseCode();
                            outputStream.flush();
                            outputStream.close();
                            fileInputStream.close();
                            urlConnection.disconnect();
                            Message msg = new Message();
                            msg.what = UPLOAD_SUCCESSED;
                            handler.sendMessage(msg);

                        }
                        else System.out.println("Dir not found.");
                    }
                    else System.out.println("File not found");

                }catch (Exception e){
                    System.out.println(e.toString());
                }
            }
        };
        new Thread(runnable).start();
    }*/

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
                    CloudActivity.instance.finish();
                    Intent intent = new Intent(VideoCaptureActivity.this,CloudActivity.class);
                    intent.putExtra("USER_NAME",userName);
                    intent.putExtra("FILE_TYPE","all");
                    intent.putExtra("ORDER","filename");
                    startActivity(intent);
                    finish();
                    //上传结束后返回显示界面

                } else if(check_result.equals("name conflict.")) {
                    alert( "Warning", "File is included in the server,\nPlease change a name." );
                }
                pdialog.hide();
            }
        }.execute("");
    }
    protected void alert(String title, String mymessage){
        new android.support.v7.app.AlertDialog.Builder(this)
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

    public File makeNewFile(String path,String file_name){
        fileName = file_name+"(1)";
        File file = new File(path+fileName+".mp4");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            makeNewFile(path,fileName);
        }
        return file;
    }

    public void saveVideo(final int check){
        //新建一个alertDialog对文件进行重命名
        final EditText name = new EditText(VideoCaptureActivity.this);
        //选中文字
        String newTemp = temp.getName().replace(".mp4","");
        name.setText(newTemp);
        Selection.selectAll(name.getText());
        final AlertDialog dialog = new AlertDialog.Builder(VideoCaptureActivity.this).setTitle("Rename the vedio").setView(name).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast toast = Toast.makeText(VideoCaptureActivity.this,"Delete temp file successfully!",Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                temp.delete();
                //跳转
                finish();
            }
        }).setNegativeButton("Sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //获得路径
                if(name.getText()!=null){
                    fileName = name.getText().toString();
                    String path = Environment.getExternalStorageDirectory()+"/saveCapture/";

                    File desDir = new File(path);
                    if(!desDir.exists()){
                        desDir.mkdir();
                    }
                    //新建文件
                    File file = new File(desDir+"/"+fileName+".mp4");
                    if(!file.exists()){
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        //file = makeNewFile(file.getPath(),fileName);
                    }
                    temp.renameTo(file);
                    Toast toast = Toast.makeText(VideoCaptureActivity.this,"Save Success",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    if(check ==1){
                        //uploadVideos();
                        upload(userName,fileType,path + fileName+".mp4");
                    }
                }else{
                    Toast.makeText(VideoCaptureActivity.this,"empty input!",Toast.LENGTH_SHORT);
                    if(check==1){
                        saveVideo(1);
                    }
                    else{
                        saveVideo(0);
                    }
                }
            }
        }).show();
    }

}