package com.lue.live.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lue.live.R;
import com.lue.live.activity.PanframePlayActivity;
import com.lue.live.activity.ViewFileActivity;
import com.lue.live.adapter.LocalGridAdapter;
import com.lue.live.sql.MyDatabaseHelper;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import android.support.design.widget.FloatingActionButton;

/**
 * Created by Lue on 2016/7/18.
 */
public class LocalFragment extends Fragment implements AdapterView.OnItemClickListener
{
    //UI展示相关
    private View rootview;
    private ProgressBar progressBar;
    private TextView textView;
    private GridView gridView;
    private LocalGridAdapter adapter;
    private Activity mActivity;

    //网格布局源数据
    private ArrayList<Bitmap> coverages = new ArrayList<>();
    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>();
    private ArrayList<Double> ratios = new ArrayList<>();

    //文件选择结果
    public static final int RESULT_OK = 1;
    public static final int RESULT_NO = 0;


    //数据库相关
    private static final String DB_NAME = "MyLocalMovies.db";
    public static final String TABLE_NAME = "Movies";
    private static final int VERSION = 1;
    private MyDatabaseHelper dbhelper;
    private SQLiteDatabase database;
    private ContentValues contentValues;

    //线程池
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();


    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            int what = msg.what;
            if (what == 1)
            {
                progressBar.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);

                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mActivity = this.getActivity();

        init();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return rootview;
    }

    /**
     * 初始化布局以及读取本地数据库
     */
    private void init()
    {
        rootview = LayoutInflater.from(mActivity).inflate(R.layout.fragment_local, null);
        progressBar = (ProgressBar) rootview.findViewById(R.id.progressbar);
        textView = (TextView) rootview.findViewById(R.id.textview);
        gridView = (GridView) rootview.findViewById(R.id.grid_local);
        gridView.setOnItemClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) rootview.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mActivity, ViewFileActivity.class);
                startActivityForResult(intent, Activity.RESULT_FIRST_USER);
            }
        });
        fab.attachToListView(gridView);


//        Resources resources = getResources();
//        Bitmap bmp = BitmapFactory.decodeResource(resources, R.mipmap.movie);
//        coverages.add(bmp);
//        titles.add("添加文件或文件夹");

        adapter = new LocalGridAdapter(mActivity, coverages, titles);
        gridView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        textView.setText("正在加载历史记录，请稍后...");
        progressBar.setIndeterminate(true);

        dbhelper = new MyDatabaseHelper(mActivity, DB_NAME, null, VERSION);
        database = dbhelper.getWritableDatabase();
        contentValues = new ContentValues();

        queryAllStored();
    }


    public static boolean isSdCardExist()
    {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public static String getSdCardPath()
    {
        boolean exist = isSdCardExist();
        String sdpath = "";
        if (exist)
        {
            sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        else
        {
            sdpath = "不适用";
        }
        return sdpath;
    }


    /**
     * 加载本地视频的缩略图和标题
     */
    private void loadLocal(final ArrayList<String> paths)
    {
        singleThreadExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                for (String filepath : paths)
                {
                    MediaMetadataRetriever media = new MediaMetadataRetriever();
                    try
                    {
                        media.setDataSource(filepath);
                        Bitmap bitmap = media.getFrameAtTime();
                        int height = bitmap.getHeight();
                        int width = bitmap.getWidth();
                        double ratio = (double)height/(double)width;
                        ratios.add(ratio);
                        coverages.add(bitmap);
                        titles.add(getMovieTitle(filepath));
                    }
                    catch (RuntimeException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            media.release();
                        }
                        catch (RuntimeException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                //新线程中加载本地视频完毕，向主线程发送消息，告知更新UI
                Message message = handler.obtainMessage();
                message.what = 1;
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        String path = paths.get(position);
        Intent intent = new Intent(mActivity, PanframePlayActivity.class);
        intent.putExtra("path", path);
        //注意titles比path多一个
        intent.putExtra("title", titles.get(position));
        intent.putExtra("ratio", ratios.get(position));
        startActivity(intent);
    }


    /**
     * 从浏览文件Activity中获取结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
        {
            Bundle bundle = data.getExtras();
            ArrayList<String> movie_paths = bundle.getStringArrayList("paths");
            ArrayList<String> new_paths = new ArrayList<>();

            if(movie_paths != null)
            {
                for (String path : movie_paths)
                {
                    if (!this.paths.contains(path))
                    {
                        this.paths.add(path);

                        new_paths.add(path);
                        contentValues.put("file_path", path);
                        contentValues.put("file_name", getMovieTitle(path));
                        database.insert(TABLE_NAME, null, contentValues);
                    }
                }
            }

            if (new_paths.size() > 0)
            {
                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("正在读取本地文件，请稍后...");
                progressBar.setIndeterminate(true);

                loadLocal(new_paths);
            }
        }
    }


    /**
     * 开启新线程，查询本地数据库中保存的记录，若存在记录，则调用loadLocal方法
     */
    public void queryAllStored()
    {
        singleThreadExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                final Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
                if(cursor.getCount() > 0)
                {
                    while (cursor.moveToNext())
                    {
                        paths.add(cursor.getString(0));
                    }
                }
                cursor.close();

                if(paths.size() > 0)
                {
                    loadLocal(paths);
                }
                else
                {
                    //新线程中加载本地视频完毕，向主线程发送消息，告知更新UI
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        });
    }


    /**
     * 将文件名后面的.mp4去掉
     *
     * @param str
     * @return
     */
    private static String getMovieTitle(String str)
    {
        int index1 = str.lastIndexOf("/");
        int index2 = str.indexOf(".mp4");
        return str.substring(index1+1, index2);
    }

}
