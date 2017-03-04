package hk.hku.cs.videocapture;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener{

    public static final String USER_NAME = "USER_NAME";

    public static final String PASSWORD = "PASSWORD";

    private static final String LOGIN_URL = "http://i.cs.hku.hk/~yzhi/login.php";

    private EditText editTextUserName;
    private EditText editTextPassword;

    private Button buttonLogin;
    private Button buttonChangePwd;
    private Button buttonRegister;
    private CheckBox RememberChecker;
    private SharedPreferences login_sp;
    private MCrypt mCrypt=new MCrypt();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserName = (EditText) findViewById(R.id.username);
        editTextPassword = (EditText) findViewById(R.id.password);

        buttonLogin = (Button) findViewById(R.id.buttonUserLogin);

        buttonLogin.setOnClickListener(this);

        buttonChangePwd = (Button) findViewById(R.id.buttonChangePwd);
        buttonChangePwd.setOnClickListener(this);

        buttonRegister = (Button) findViewById(R.id.buttonToRegister);
        buttonRegister.setOnClickListener(this);

        RememberChecker= (CheckBox) findViewById(R.id.Login_Remember);
        login_sp=getSharedPreferences("userinfo",0);
        String name=login_sp.getString("USER_NAME", "");
        String password=login_sp.getString("PASSWORD", "");

       boolean choseRemember=login_sp.getBoolean("RememberCheck",false);

        if(choseRemember){
            editTextUserName.setText(name);
            editTextPassword.setText(password);
            RememberChecker.setChecked(true);
        }

    }


    private void login() throws Exception {
        String username = editTextUserName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        SharedPreferences.Editor editor=login_sp.edit();
        editor.putString("USER_NAME",username);
        editor.putString("PASSWORD",password);
        if(RememberChecker.isChecked()){
            editor.putBoolean("RememberCheck",true);
        }else{
            editor.putBoolean("RememberCheck",false);
        }
        editor.commit();
        password=MCrypt.bytesToHex(mCrypt.encrypt(password));
        userLogin(username,password);
    }

    private void userLogin(final String username, final String password){
        class UserLoginClass extends AsyncTask<String,Void,String>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ActivityLogin.this,"Please Wait",null,true,true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if(s.equalsIgnoreCase("success")){
                    Intent intent = new Intent(ActivityLogin.this,CloudActivity.class);
                    intent.putExtra(USER_NAME,username);
                    intent.putExtra("FILE_TYPE","all");
                    intent.putExtra("ORDER","filename");
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ActivityLogin.this,s,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                HashMap<String,String> data = new HashMap<>();
                data.put("username",params[0]);
                data.put("password",params[1]);

                RegisterUserClass ruc = new RegisterUserClass();

                String result = ruc.sendPostRequest(LOGIN_URL,data);

                return result;
            }
        }
        UserLoginClass ulc = new UserLoginClass();
        ulc.execute(username,password);
    }

    private void changePwd() throws Exception {
        String username = editTextUserName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        SharedPreferences.Editor editor=login_sp.edit();
        editor.putString("USER_NAME",username);
        editor.putString("PASSWORD",password);
        if(RememberChecker.isChecked()){
            editor.putBoolean("RememberCheck",true);
        }else{
            editor.putBoolean("RememberCheck",false);
        }
        editor.commit();
        password=MCrypt.bytesToHex(mCrypt.encrypt(password));
        userChangePwd(username,password);
    }
    private void userChangePwd(final String username, final String password){
        class UserLoginClass extends AsyncTask<String,Void,String>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ActivityLogin.this,"Please Wait",null,true,true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if(s.equalsIgnoreCase("success")){
                    Intent intent = new Intent(ActivityLogin.this,ChangePwd.class);
                    intent.putExtra(USER_NAME,username);
                    startActivity(intent);
                }else{
                    Toast.makeText(ActivityLogin.this,s,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                HashMap<String,String> data = new HashMap<>();
                data.put("username",params[0]);
                data.put("password",params[1]);

                RegisterUserClass ruc = new RegisterUserClass();

                String result = ruc.sendPostRequest(LOGIN_URL,data);

                return result;
            }
        }
        UserLoginClass ulc = new UserLoginClass();
        ulc.execute(username,password);
    }

    @Override
    public void onClick(View v) {
        if(v == buttonLogin){
            try {
                login();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(v == buttonChangePwd){
            Intent intent = new Intent(ActivityLogin.this,ChangePwd.class);
            startActivity(intent);
        }
        if(v == buttonRegister){
            Intent intent = new Intent(ActivityLogin.this,MainActivity.class);
            startActivity(intent);
        }
    }
}
