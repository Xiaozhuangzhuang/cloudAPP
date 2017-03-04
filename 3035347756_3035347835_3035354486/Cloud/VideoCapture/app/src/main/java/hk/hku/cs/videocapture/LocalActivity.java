package hk.hku.cs.videocapture;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocalActivity extends AppCompatActivity {

    private String userName;
    private File[] fileList;
    private Handler handler;
    private final int SYNC_SUCCESS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        Button button = (Button) findViewById(R.id.synchronize);

        userName = getIntent().getStringExtra("USER_NAME");
        ListView listView = (ListView) findViewById(R.id.local_list);

        if(fileList.length>0){
            String prePath = Environment.getExternalStorageDirectory()+"/saveCapture/";
            File dirFile = new File(prePath);
            if(dirFile.exists()){
                fileList = dirFile.listFiles();
                listView.setAdapter(new LocalAdapter(fileList, LocalActivity.this));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String[] data = fileList[position].getName().split("\\.");
                        Bundle bundle = new Bundle();
                        bundle.putString("FILE_TYPE",data[data.length-1]);
                        bundle.putString("USER_NAME", userName);
                        bundle.putString("FILE_NAME", fileList[position].getName());
                        Intent intent = new Intent(LocalActivity.this, PlayLocalActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
            else{
                System.out.println("Dir doesn't exists.");
            }
        }
        else{
            TextView textView = new TextView(LocalActivity.this);
            textView.setText("There is no file need to be upload");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            textView.setLayoutParams(layoutParams);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_local);
            linearLayout.addView(textView);

        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 0;i<fileList.length-1;i++){
                    //获取后缀名
                    String fileType = fileList[i].getName().substring(fileList[i].getName().lastIndexOf(".") + 1,
                            fileList[i].getName().length()).toLowerCase();
                    upload(userName,fileType,fileList[i].getPath());
                }
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
