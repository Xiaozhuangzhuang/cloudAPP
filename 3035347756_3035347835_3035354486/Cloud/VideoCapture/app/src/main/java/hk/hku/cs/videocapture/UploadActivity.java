package hk.hku.cs.videocapture;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by HP on 2016/11/26.
 */

public class UploadActivity extends AppCompatActivity {
    private String userName;
    private String fileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        userName = getIntent().getStringExtra("USER_NAME");
        fileType = getIntent().getStringExtra("FILE_TYPE");

        Intent intent = new Intent();
        intent.setType(fileType+"/*");
                        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select"), 1);

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getData();
            Log.e("uri", uri.toString());
            final String srcPath = uri.getPath();
            upload(userName,fileType,srcPath);
        }
        else{
            Intent intent = new Intent(UploadActivity.this,CloudActivity.class);
            intent.putExtra("USER_NAME",userName);
            intent.putExtra("FILE_TYPE","all");
            intent.putExtra("ORDER","filename");
            startActivity(intent);
            finish();
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
                        filename = filename.replace(" ","");
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
                    //结束上一个界面
                    CloudActivity.instance.finish();
                    Intent intent = new Intent(UploadActivity.this,CloudActivity.class);
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

