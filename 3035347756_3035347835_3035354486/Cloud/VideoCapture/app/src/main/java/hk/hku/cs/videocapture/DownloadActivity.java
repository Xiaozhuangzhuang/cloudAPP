package hk.hku.cs.videocapture;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class DownloadActivity extends AppCompatActivity {

    private String userName;
    private File[] fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        userName = getIntent().getStringExtra("USER_NAME");
        String prePath = Environment.getExternalStorageDirectory() + "/videoCapture/";
        File dirFile = new File(prePath);
        if (dirFile.exists()) {
            fileList = dirFile.listFiles();

            ListView listView = (ListView) findViewById(R.id.local_list);
            listView.setAdapter(new LocalAdapter(fileList, DownloadActivity.this));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String[] data = fileList[position].getName().split("\\.");
                    Bundle bundle = new Bundle();
                    bundle.putString("FILE_TYPE", data[data.length - 1]);
                    bundle.putString("FILE_NAME", fileList[position].getName());
                    Intent intent = new Intent(DownloadActivity.this, PlayDownloadActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(DownloadActivity.this).setMessage("Do you want to delete it?");
                    dialog.setPositiveButton("No", null);
                    dialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            fileList[position].delete();
                            Toast.makeText(DownloadActivity.this,"Delete successfully",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DownloadActivity.this,DownloadActivity.class);
                            intent.putExtra("USER_NAME",userName);
                            startActivity(intent);
                            finish();
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
        } else {
            System.out.println("Dir doesn't exists.");
        }
    }
}
