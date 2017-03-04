package hk.hku.cs.videocapture;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PictureCaptureActivity extends AppCompatActivity {
    //private static final String TAG = BrowserActivity.class.getSimpleName();
    private Button uploadImage;
    private Button saveImage;
    private ImageView imageView;
    private static final int CAMERA_IMAGE_REQUEST = 11111;
    private static final int IMAGE_CAPTURE_PERMISSION = 2222;
    private String srcPath;
    private String userName;
    private String fileName;
    private String fileType;
    private File temp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_capture);
        userName =getIntent().getStringExtra("USER_NAME");
        uploadImage = (Button)findViewById(R.id.imageUpload);
        saveImage = (Button) findViewById(R.id.imageSave);
        imageView = (ImageView)findViewById(R.id.imageview);
        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveVideo(0);
            }
        });
        uploadImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveVideo(1);
            }
        });

        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(PictureCaptureActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(PictureCaptureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(PictureCaptureActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(PictureCaptureActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }

        if(permissions.size() > 0) {
            String[] permiss = permissions.toArray(new String[0]);

            ActivityCompat.requestPermissions(PictureCaptureActivity.this, permiss,
                    IMAGE_CAPTURE_PERMISSION);
        } else {
            StartPictureCapture();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_CAPTURE_PERMISSION) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                StartPictureCapture();
            }
            else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }

    private void StartPictureCapture(){
        Uri viduri;
        viduri = getOutputMediaFileUri();
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, viduri);
        startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
    }

    private Uri getOutputMediaFileUri() {
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath()+"/saveCapture");
            if (! mediaStorageDir.exists()) {
                if(! mediaStorageDir.mkdirs()){
                    //Log.e(TAG, "Failed to create directory.");
                    return null;
                }
            }
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
            String path = mediaStorageDir.getPath() + File.separator;
            temp = new File(path + "IMG_" + timestamp + ".jpg");
            srcPath = temp.getPath();
            //Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            return Uri.fromFile(temp);
        }
        else {
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            FileInputStream is=null;
            try {
                // 获取输入流
                is = new FileInputStream(srcPath);
                // 把流解析成bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                // 缩放图片
                Bitmap resizeBitmap = resizeBitmap(bitmap, 640);
                imageView.setImageBitmap(resizeBitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                // 关闭流
                try {
                    if(is!=null)
                        is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        else{
            finish();
        }
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float temp = ((float)height) / ((float)width);
        int newHeight = (int)((newWidth) * temp);
        float scaleWidth = ((float)newWidth) / width;
        float scaleHeight = ((float)newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }

    protected void upload(final String fileType, final String userName ,final String srcPath){
        final ProgressDialog pdialog = new ProgressDialog(this);

        pdialog.setCancelable(false);
        pdialog.setMessage("uploading ...");
        pdialog.show();
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... arg0) {
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
                    String filename=fileType+"_"+userName+"_"+srcPath.substring(srcPath.lastIndexOf("/") + 1) ;
                    ds.writeBytes("Content-Disposition: form-data;name=\"file\";filename=\""+filename+"\""+end);
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
                        // 取回响应的结果
                        InputStream is = conn_cshomepage.getInputStream();
                        int ch;
                        StringBuffer b = new StringBuffer();
                        while ((ch = is.read()) != -1) {
                            b.append((char) ch);
                        }
                    }
                    conn_cshomepage.disconnect();

                }  catch (Exception e)
                {
                    e.printStackTrace();
                    setTitle(e.getMessage());
                }
                return null;
            }
            @Override
            protected void onPostExecute(String result) {
                pdialog.hide();
                //结束上一个界面
                if(CloudActivity.instance!=null)
                    CloudActivity.instance.finish();
                Intent intent = new Intent(PictureCaptureActivity.this,CloudActivity.class);
                intent.putExtra("USER_NAME",userName);
                intent.putExtra("FILE_TYPE","all");
                intent.putExtra("ORDER","filename");
                startActivity(intent);
                finish();
                super.onPostExecute(result);
            }
        }.execute("");
    }

    public void saveVideo(final int check){
        //新建一个alertDialog对文件进行重命名
        final EditText name = new EditText(PictureCaptureActivity.this);
        //选中文字
        String newTemp = temp.getName().replace(".jpg","");
        name.setText(newTemp);
        Selection.selectAll(name.getText());
        final AlertDialog dialog = new AlertDialog.Builder(PictureCaptureActivity.this).setTitle("Rename the picture").setView(name).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast toast = Toast.makeText(PictureCaptureActivity.this,"Delete temp file successfully!",Toast.LENGTH_LONG);
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
                    File file = new File(desDir+"/"+fileName+".jpg");
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
                    Toast toast = Toast.makeText(PictureCaptureActivity.this,"Save Success",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    if(check ==1){
                        //uploadVideos();
                        upload("image",userName,path + fileName+".jpg");
                    }
                }else{
                    Toast.makeText(PictureCaptureActivity.this,"empty input!",Toast.LENGTH_SHORT);
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
}
