package com.lue.live.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lue.live.R;
import com.lue.live.adapter.FileAdapter;
import com.lue.live.fragment.LocalFragment;
import com.lue.live.model.ScanFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by Lue on 2015/9/22.
 */
public class ViewFileActivity extends AppCompatActivity
{
    private FileAdapter adapter;
    private ArrayList<ScanFile> scanFiles = new ArrayList<>();
    private ListView lv_scan_files;

    //扫描音乐时，显示的进度dialog
    private ProgressDialog dialog;

    //记录当前路径
    private String currentPath;
    private String previousPath;

    /**********************************************************************************************/
    private Intent intent;
    private ArrayList<String> movie_paths = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i("ScanFile: ","Start!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);


        intent = getIntent();

        init();
    }

    /**
     * 添加ActionBar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_file, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        //当点击不同的menu item 是执行不同的操作
        switch (id) {
            case R.id.action_complete:
                for (ScanFile scanFile : scanFiles)
                {
                    if (scanFile.isChosen())
                    {
                        //若果是文件夹的话
                        if(scanFile.isDirectory())
                        {
                            File root_file = new File(scanFile.getFilePath());
                            File[] child_files = root_file.listFiles();

                            for (File file : child_files)
                            {
                                if (file.getName().endsWith(".mp4") || file.getName().endsWith(".avi"))
                                {
                                    movie_paths.add(file.toString());
                                }
                            }
                        }
                        else
                        {
                            if(scanFile.getFileName().endsWith(".mp4") || scanFile.getFileName().endsWith(".avi"))
                                movie_paths.add(scanFile.getFilePath());
                        }
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("paths", movie_paths);
                intent.putExtras(bundle);
                setResult(LocalFragment.RESULT_OK, intent);
                finish();
                break;
            case android.R.id.home:
                setResult(LocalFragment.RESULT_NO, intent);
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init()
    {
//        initDialog();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.mipmap.back);
        }

        lv_scan_files = (ListView) findViewById(R.id.lv_scan_files);

        adapter = new FileAdapter(this, R.layout.item_file, scanFiles);
        lv_scan_files.setAdapter(adapter);
        lv_scan_files.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                ScanFile scanFile = scanFiles.get(i);
                if (scanFile.isDirectory())
                {
                    File file = new File(scanFile.getFilePath());
                    showScanFiles(file);
                } else
                {
                    Toast.makeText(ViewFileActivity.this, "已经是一个文件...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        showScanFiles(Environment.getExternalStorageDirectory());
        adapter.notifyDataSetChanged();
        lv_scan_files.setBackgroundColor(Color.WHITE);
    }

    private void showScanFiles(File file)
    {




        scanFiles.clear();
        currentPath = file.getAbsolutePath();
        File[] files = file.listFiles();
        for (File tmp : files)
        {
            String fileName = tmp.getName();
            if (fileName.startsWith("."))
            {
                //去掉.开头的隐藏文件
                continue;
            }
            String filePath = tmp.getAbsolutePath();
            boolean isDirectory = tmp.isDirectory();
            ScanFile scanFile = new ScanFile(filePath, fileName, false, isDirectory);
            scanFiles.add(scanFile);
        }
        Collections.sort(scanFiles, new FileComparator());
        adapter.notifyDataSetChanged();
    }


    /**
     * 重写返回键
     */
    @Override
    public void onBackPressed()
    {

        if (currentPath.endsWith("/storage"))
        {
            setResult(LocalFragment.RESULT_NO, intent);
            finish();
        }
        else
        {
            previousPath = getPreviousPath(currentPath);

            if(Build.VERSION.SDK_INT >= 23)
                requestPermission();
            else
                showScanFiles(new File(previousPath));
//
        }
    }

    /**
     * 根据当前路径得到前一个路径
     *
     * @param path
     * @return
     */
    private String getPreviousPath(String path)
    {
        int cnt = 0;
        for (int i = 0; i < path.length(); i++)
        {
            if (path.charAt(i) == '/')
            {
                cnt++;
            }
        }
        int tmp = 0;
        for (int i = 0; i < path.length(); i++)
        {
            tmp++;
            if (path.charAt(i) == '/')
            {
                cnt--;
            }
            if (cnt == 0)
            {
                break;
            }
        }
        return path.substring(0, tmp - 1);
    }
//
//    private void initDialog()
//    {
//        dialog = new ProgressDialog(this);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setMessage("扫描中，请稍后...");
//        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//    }



    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;

    public void requestPermission(){
        //判断当前Activity是否已经获得了该权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"please give me the permission",Toast.LENGTH_SHORT).show();
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQ_CODE: {
                // 如果请求被拒绝，那么通常grantResults数组为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请成功，进行相应操作
//                    createFile("hello.txt");
                    showScanFiles(new File(previousPath));
                } else {
                    //申请失败，可以继续向用户解释。
                }
                return;
            }
        }
    }


    //扫描到文件，按文件名排序用的
    private class FileComparator implements Comparator<ScanFile>
    {

        @Override
        public int compare(ScanFile lhs, ScanFile rhs)
        {
            return lhs.getFileName().toLowerCase().compareTo(rhs.getFileName().toLowerCase());
        }
    }
}
