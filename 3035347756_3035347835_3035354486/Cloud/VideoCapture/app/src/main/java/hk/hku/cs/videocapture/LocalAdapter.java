package hk.hku.cs.videocapture;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by HP on 2016/12/2.
 */

public class LocalAdapter extends BaseAdapter{
    private int count = 0;
    private File[] fileList;
    private LayoutInflater mInflater;
    private Context myContext;
    ViewGroup vg;

    public LocalAdapter(File[] files, Context context){
        mInflater = LayoutInflater.from(context);
        fileList = files;
        count = files.length;
        myContext = context;
    }
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = mInflater.inflate(R.layout.list_layout,null);

        //获取屏幕宽度，取12分之1为view的宽度和长度
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) myContext.getSystemService(myContext.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.heightPixels/12;
        int height = width;

        //获取文件名后缀
        String[] post = fileList[position].getName().split("\\.");

        //设置ImageView
        ImageView img = (ImageView) convertView.findViewById(R.id.img);
        //为不同的后缀设置图片资源
        if(post.length>1){
            if(post[post.length-1].equals("mp3")){
                img.setImageResource(R.drawable.mp3);
            }
            else if(post[post.length-1].equals("mp4")){
                img.setImageResource(R.drawable.mp4);
            }
            else if(post[post.length-1].equals("png")){
                img.setImageResource(R.drawable.png);
            }
            else if(post[post.length-1].equals("jpg")){
                img.setImageResource(R.drawable.jpg);
            }
            else if(post[post.length-1].equals("jpeg")){
                img.setImageResource(R.drawable.jpeg);
            }
            else if(post[post.length-1].equals("avi")){
                img.setImageResource(R.drawable.avi);
            }
            else if(post[post.length-1].equals("ppt")){
                img.setImageResource(R.drawable.ppt);
            }
            else if(post[post.length-1].equals("pdf")){
                img.setImageResource(R.drawable.pdf);
            }
            else if(post[post.length-1].equals("wav")){
                img.setImageResource(R.drawable.wav);
            }
            else if(post[post.length-1].equals("wma")){
                img.setImageResource(R.drawable.wma);
            }
            else if(post[post.length-1].equals("xls")){
                img.setImageResource(R.drawable.xls);
            }
            else{
                System.out.println("format error");
            }
        }else{
            img.setImageResource(R.drawable.pdefault);
        }
        //设置图片大小
        ViewGroup.LayoutParams params = img.getLayoutParams();
        params.height = height;
        params.width = width;
        img.setLayoutParams(params);
        TextView textView = (TextView) convertView.findViewById(R.id.filename);
        textView.setText(fileList[position].getName());

        return convertView;
    }
}
