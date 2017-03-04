package hk.hku.cs.videocapture;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by HP on 2016/11/24.
 */

public class CloudActivity extends AppCompatActivity{

    private String userName;
    private String fileType;
    private String orderMethod;
    private final int JSON_COMING = 1;
    private int JSON_SUCCESS = 100;
    private int DELETE_SUCCESS = 200;
    private Handler handler;
    private ArrayList<String> dataArray;
    private File[] fileList;
    private int count;//计数
    public static CloudActivity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        userName = getIntent().getStringExtra("USER_NAME");
        fileType = getIntent().getStringExtra("FILE_TYPE");
        orderMethod = getIntent().getStringExtra("ORDER");
        instance = this;

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == JSON_SUCCESS) {
                    Toast.makeText(CloudActivity.this, "download sucess in /videoCapture!", Toast.LENGTH_SHORT).show();
                }
                else if(msg.what==DELETE_SUCCESS) {
                    Toast.makeText(CloudActivity.this, "delete successfuly!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                    intent.putExtra("USER_NAME",userName);
                    intent.putExtra("FILE_TYPE",fileType);
                    intent.putExtra("ORDER","filename");
                    startActivity(intent);
                    finish();
                }
                else if (msg.what == JSON_COMING) {
                    if(dataArray.size()!=0){

                        ListView listView = (ListView) findViewById(R.id.video_list);
                        listView.setAdapter(new ListAdapter(dataArray, CloudActivity.this));

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                String[] data = dataArray.get(position).split("\\.");
                                Bundle bundle = new Bundle();
                                bundle.putString("FILE_TYPE",data[data.length-1]);
                                bundle.putString("USER_NAME", userName);
                                bundle.putString("FILE_NAME", dataArray.get(position));
                                Intent intent = new Intent(CloudActivity.this, PlayVideoActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });

                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int position, long l) {
                                final View popView = LayoutInflater.from(CloudActivity.this).inflate(R.layout.pop_end_menu, null);
                                final RelativeLayout total = (RelativeLayout) findViewById(R.id.total);
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200);
                                popView.setLayoutParams(layoutParams);
                                total.addView(popView);
                                //获得屏幕底部坐标


                                final int maxHeight = total.getHeight();
                                TranslateAnimation translateAnimation = new TranslateAnimation(0,0,maxHeight,maxHeight-view.getHeight());
                                translateAnimation.setDuration(400);
                                translateAnimation.setFillAfter(true);
                                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        popView.clearAnimation();
                                        popView.layout(0, maxHeight-view.getHeight(),view.getWidth(), maxHeight);
                                    }
                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });
                                popView.startAnimation(translateAnimation);

                                TextView pTextView = (TextView) popView.findViewById(R.id.play);
                                pTextView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String[] data = dataArray.get(position).split("\\.");
                                        //为fileType进行分类
                                        Bundle bundle = new Bundle();
                                        bundle.putString("FILE_TYPE",data[data.length-1]);
                                        bundle.putString("USER_NAME", userName);
                                        bundle.putString("FILE_NAME", dataArray.get(position));
                                        Intent intent = new Intent(CloudActivity.this, PlayVideoActivity.class);
                                        intent.putExtras(bundle);
                                        startActivity(intent);

                                        total.removeView(popView);
                                    }
                                });

                                TextView doTextView = (TextView) popView.findViewById(R.id.download_text);
                                doTextView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Runnable runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    String pre = "http://i.cs.hku.hk/~zjzeng/upload/";
                                                    //建立httpUrlConnection连接，并进行设置
                                                    String newfileName = dataArray.get(position).replace(" ", "%20");
                                                    URL url = new URL(pre + userName + "/" + newfileName);

                                                    //URL url = new URL("http://i.cs.hku.hk/~zjzeng/upload/zhi/Ariana%20Grande%20Iggy%20Azalea%20-%20Problem%20(feat.%20Iggy%20Azalea).mp3");
                                                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                                                    //服务器正常响应则开始接收文件
                                                    if (urlConnection.getResponseCode() == 200) {
                                                        File destDir = new File(Environment.getExternalStorageDirectory() + "/videoCapture/");
                                                        if (!destDir.exists()) {
                                                            destDir.mkdirs();
                                                        }
                                                        BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                                                        FileOutputStream fileOutputStream = null;

                                                        File file = new File(destDir + "/" + dataArray.get(position));
                                                        if (!file.exists()) {
                                                            file.createNewFile();
                                                            fileOutputStream = new FileOutputStream(file, true);
                                                        }else{
                                                            String[] spilt = dataArray.get(position).split("\\.");
                                                            File newFile = new File(destDir + "/" + spilt[0]+"(1)"+"."+spilt[1]);
                                                            newFile.createNewFile();
                                                            fileOutputStream = new FileOutputStream(newFile, true);
                                                        }

                                                        //设置读取每次读取长度以及当前读取长度
                                                        byte[] by = new byte[1024];
                                                        int len = 0;
                                                        ;
                                                        int total = 0;
                                                        //读文件
                                                        while ((len = inputStream.read(by)) != -1) {
                                                            fileOutputStream.write(by, 0, len);
                                                            total = total + len;
                                                        }
                                                        inputStream.close();
                                                        fileOutputStream.close();
                                                        Message msg = new Message();
                                                        msg.what = JSON_SUCCESS;
                                                        handler.sendMessage(msg);
                                                        urlConnection.disconnect();

                                                    } else {
                                                        Message msg = new Message();
                                                        msg.what = -1;
                                                        urlConnection.disconnect();
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e.toString());
                                                }
                                            }

                                        };
                                        new Thread(runnable).start();
                                        total.removeView(popView);
                                    }
                                });
                                TextView deTextView = (TextView) popView.findViewById(R.id.delete_text);
                                deTextView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Runnable runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    String newfileName = dataArray.get(position).replace(" ", "%20");
                                                    String path = "http://i.cs.hku.hk/~zjzeng/delete.php?username=" + userName+ "&filename=" + newfileName;
                                                    String check_result;
                                                    URL url = new URL(path);
                                                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//使用url打开一个链接
                                                    urlConnection.setDoInput(true); //允许输入流
                                                    urlConnection.setDoOutput(true); //允许输出流
                                                    urlConnection.setUseCaches(false); //不使用缓冲

                                                    int code = urlConnection.getResponseCode();
                                                    if (code == 200) {
                                                        InputStream is = urlConnection.getInputStream();
                                                        int ch;
                                                        StringBuffer b = new StringBuffer();
                                                        while ((ch = is.read()) != -1) {
                                                            b.append((char) ch);
                                                        }
                                                        check_result = b.toString();
                                                        if (check_result.equals("delete successfuly.")) {
                                                            Message msg = new Message();
                                                            msg.what = DELETE_SUCCESS;
                                                            handler.sendMessage(msg);
                                                        }
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        };
                                        new Thread(runnable).start();
                                        total.removeView(popView);
                                    }
                                });
                                return true;
                            }
                        });
                    }
                }
                else{

                }
            }
        };
        setOnClick();
        getFileName();
    }

    public void getFileName(){
        Runnable runnable = new Runnable(){
            @Override
            public void run(){
                try{
                    //uploadVideos();
                    URL url;
                    url = new URL("http://i.cs.hku.hk/~zjzeng/listview.php?username="+userName+"&filetype="+fileType+"&orderby="+orderMethod);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//使用url打开一个链接
                    urlConnection.setDoInput(true); //允许输入流
                    urlConnection.setDoOutput(true); //允许输出流
                    urlConnection.setUseCaches(false); //不使用缓冲
                    urlConnection.connect();
                    int code = urlConnection.getResponseCode();
                    if(code==200){
                        //定义数据输入流以及文件输出流
                        InputStream inputStream = urlConnection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(inputStream);
                        BufferedReader bufferReader = new BufferedReader(isr);
                        //接收流信息
                        String inputLine  = "";
                        String data = "";
                        while((inputLine = bufferReader.readLine()) != null){
                            data += inputLine + "\n";
                        }

                        String[] dataC = data.split(", ");
                        dataArray = new ArrayList<String>();
                        //将每一个视频名称存入String Array
                        for(int i=0;i<dataC.length;i++){
                            dataC[i] = dataC[i].replace("\"","");
                            dataC[i] = dataC[i].replace("\n","");
                            dataC[i] = dataC[i].replace("[","");//网络返回数据存在[及],"
                            dataC[i] = dataC[i].replace("]","");
                            if(!dataC[i].equals(".")&&!dataC[i].equals("..")&&!dataC[i].equals("")){
                                dataArray.add(dataC[i]);
                            }
                        }
                        urlConnection.disconnect();
                        Message msg = new Message();
                        msg.what = JSON_COMING;
                        msg.obj = dataArray;
                        handler.sendMessage(msg);
                        //getVideos();
                    }
                }catch (Exception e){
                    System.out.println(e.toString());
                }
            }
        };
        new Thread(runnable).start();
    }

    public void setOnClick() {
        TextView txtCloud = (TextView) findViewById(R.id.text_cloud);
        TextView txtCapture = (TextView) findViewById(R.id.text_capture);
        TextView txtUpload = (TextView) findViewById(R.id.text_upload);
        final ImageView oImage = (ImageView) findViewById(R.id.order);
        ImageView sImage = (ImageView) findViewById(R.id.sync);
        ImageView dImage = (ImageView) findViewById(R.id.downloadButton);


        oImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(CloudActivity.this, oImage);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.ordermenu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.order_by_name){
                            orderMethod = "filename";
                        }
                        else if(item.getItemId()==R.id.order_by_time){
                            orderMethod = "createtime";
                        }
                        else{
                            orderMethod = "filesize";
                        }
                        Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        intent.putExtra("ORDER",orderMethod);
                        startActivity(intent);
                        finish();
                        return true;
                    }
                });
                popup.show();
            }
        });

        sImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String prePath = Environment.getExternalStorageDirectory()+"/saveCapture/";
                File dirFile = new File(prePath);
                if(dirFile.exists()){
                    fileList = dirFile.listFiles();
                }
                if(fileList!=null)
                {
                    count = 0;
                    for(int i = 0;i<fileList.length-1;i++){
                        //获取后缀名
                        String fileType = fileList[i].getName().substring(fileList[i].getName().lastIndexOf(".") + 1,
                                fileList[i].getName().length()).toLowerCase();
                        if(fileType.equals("mp3")||fileType.equals("wma")){
                            fileType = "audio";
                        }
                        else if(fileType.equals("jpg")||fileType.equals("png")){
                            fileType = "image";
                        }
                        else if (fileType.equals("mp4")){
                            fileType = "video";
                        }
                        else{
                            fileType = "text";
                        }
                        upload(userName,fileType,fileList[i].getPath());
                    }
                    Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                    intent.putExtra("USER_NAME",userName);
                    intent.putExtra("FILE_TYPE",fileType);
                    intent.putExtra("ORDER","filename");
                    startActivity(intent);
                }
            }
        });

        dImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CloudActivity.this,DownloadActivity.class);
                startActivity(intent);
            }
        });



        txtCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出对话框，选择显示不同文件类型
                LayoutInflater classiInflater = LayoutInflater.from(CloudActivity.this);
                RelativeLayout classiLayout = (RelativeLayout) classiInflater.inflate(R.layout.dialog_classi_layout,null);
                AlertDialog classiDialog = new AlertDialog.Builder(CloudActivity.this).create();
                classiDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                classiDialog.setView(classiLayout);
                classiDialog.show();

                //获取imageView
                ImageView classiDocImage = (ImageView) classiDialog.findViewById(R.id.classi_doc);
                ImageView classiMusicImage = (ImageView) classiDialog.findViewById(R.id.classi_music);
                ImageView classiVideoImage = (ImageView) classiDialog.findViewById(R.id.classi_video);
                ImageView classiPictureImage = (ImageView) classiDialog.findViewById(R.id.classi_picture);
                ImageView classiAllImage = (ImageView) classiDialog.findViewById(R.id.classi_all);

                //设置点击事件
                classiDocImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "text";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        intent.putExtra("ORDER","filename");
                        startActivity(intent);
                        finish();
                    }
                });
                classiMusicImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "audio";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        intent.putExtra("ORDER","filename");
                        startActivity(intent);
                        finish();
                    }
                });
                classiPictureImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "image";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        intent.putExtra("ORDER","filename");
                        startActivity(intent);
                        finish();
                    }
                });
                classiVideoImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "video";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        intent.putExtra("ORDER","filename");
                        startActivity(intent);
                        finish();
                    }
                });
                classiAllImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "all";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,CloudActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        intent.putExtra("ORDER","filename");
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

        txtCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        txtUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出对话框，选择显示不同上传类型
                LayoutInflater uploadInflater = LayoutInflater.from(CloudActivity.this);
                RelativeLayout uploadLayout = (RelativeLayout) uploadInflater.inflate(R.layout.dialog_upload_layout,null);
                AlertDialog uploadDialog = new AlertDialog.Builder(CloudActivity.this).create();
                uploadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                uploadDialog.setView(uploadLayout);
                uploadDialog.show();

                //获取imageView
                ImageView uploadDocImage = (ImageView) uploadDialog.findViewById(R.id.upload_doc);
                ImageView uploadMusicImage = (ImageView) uploadDialog.findViewById(R.id.upload_music);
                ImageView uploadVideoImage = (ImageView) uploadDialog.findViewById(R.id.upload_video);
                ImageView uploadPictureImage = (ImageView) uploadDialog.findViewById(R.id.upload_picture);
                ImageView uploadAllImage = (ImageView) uploadDialog.findViewById(R.id.upload_all);

                //设置点击事件
                uploadDocImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "text";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,UploadActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        startActivity(intent);

                    }
                });
                uploadMusicImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "audio";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,UploadActivity.class);
                        intent.setType(fileType);
                        /* 使用Intent.ACTION_GET_CONTENT这个Action */
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        startActivity(intent);
                    }
                });
                uploadPictureImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "image";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,UploadActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        startActivity(intent);
                    }
                });
                uploadVideoImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "video";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,UploadActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        startActivity(intent);
                    }
                });
                uploadAllImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileType = "*";
                        //初始化云视频fragment或显示云视频fragment
                        Intent intent = new Intent(CloudActivity.this,UploadActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        intent.putExtra("FILE_TYPE",fileType);
                        startActivity(intent);
                    }
                });
            }
        });
        txtCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出对话框
                final LayoutInflater uploadInflater = LayoutInflater.from(CloudActivity.this);
                RelativeLayout uploadLayout = (RelativeLayout) uploadInflater.inflate(R.layout.dialog_choose_layout,null);
                AlertDialog captureDialog = new AlertDialog.Builder(CloudActivity.this).create();
                captureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                captureDialog.setView(uploadLayout);
                captureDialog.show();

                ImageView captureVideoImage = (ImageView) captureDialog.findViewById(R.id.choose_video);
                ImageView capturePictureImage = (ImageView) captureDialog.findViewById(R.id.choose_picture);

                captureVideoImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CloudActivity.this,VideoCaptureActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        startActivity(intent);
                    }
                });
                capturePictureImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CloudActivity.this,PictureCaptureActivity.class);
                        intent.putExtra("USER_NAME",userName);
                        startActivity(intent);
                    }
                });
            }
        });
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
                        count = count+1;
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
                        filename = filename.replace("　","");
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
                return Integer.toString(count);
            }
            @Override
            protected void onPostExecute(String result) {
                //Toast.makeText(CloudActivity.this,check_result,Toast.LENGTH_SHORT).show();
                pdialog.hide();
                if(count==fileList.length-1){
                    finish();
                }
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
        // add context menu item
        menu.add(0,1,Menu.NONE,"Play");
        menu.add(0, 2, Menu.NONE, "Download");
        menu.add(0, 3, Menu.NONE, "Delete");
    }



}

