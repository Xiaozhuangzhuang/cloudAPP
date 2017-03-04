package hk.hku.cs.videocapture;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChangePwd extends AppCompatActivity implements View.OnClickListener {

    private EditText userName;
    private EditText passWord;
    private EditText NewPwd_1;
    private EditText NewPwd_2;
    private Button changePwd;
    private MCrypt mCrypt=new MCrypt();


    private static final String CHANGEPWD_URL = "http://i.cs.hku.hk/~yzhi/resetpwd.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);

        userName= (EditText) findViewById(R.id._username);
        passWord=(EditText)findViewById(R.id._password);
        NewPwd_1= (EditText) findViewById(R.id.new_pwd_1);
        NewPwd_2= (EditText) findViewById(R.id.new_pwd_2);

        changePwd=(Button)findViewById(R.id.button_ChangePwd);

        changePwd.setOnClickListener(this);

    }


    private void changePwd() throws Exception {
        String name = userName.getText().toString().trim();
        String pwd = passWord.getText().toString().trim();
        pwd=MCrypt.bytesToHex(mCrypt.encrypt(pwd));
        String newpassword1 = NewPwd_1.getText().toString().trim();
        newpassword1=MCrypt.bytesToHex(mCrypt.encrypt(newpassword1));
        String newpassword2 = NewPwd_2.getText().toString().trim();
        newpassword2=MCrypt.bytesToHex(mCrypt.encrypt(newpassword2));
        userChangePwd(name,pwd,newpassword1,newpassword2);
    }

    private void userChangePwd(String username, String password,String password1, String password2) {

        String urlSuffix = "?username="+username+"&password="+password+"&password1="+password1+"&password2="+password2;
        class RegisterUser extends AsyncTask<String, Void, String> {

            ProgressDialog loading;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ChangePwd.this, "Please Wait",null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {
                String s = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(CHANGEPWD_URL+s);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String result;

                    result = bufferedReader.readLine();

                    return result;
                }catch(Exception e){
                    return null;
                }
            }
        }

        RegisterUser ru = new RegisterUser();
        ru.execute(urlSuffix);
    }

    public void onClick(View v) {
        if(v == changePwd){
            try {
                changePwd();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
