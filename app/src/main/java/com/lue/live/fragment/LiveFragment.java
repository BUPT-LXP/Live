package com.lue.live.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.lue.live.R;
import com.lue.live.activity.PanframePlayActivity;
import com.lue.live.adapter.AutoCompleteAdapter;
import com.lue.live.model.AdvancedAutoCompleteTextView;
import com.lue.live.model.SharedPrefsStrListUtil;

import java.util.ArrayList;

/**
 * Created by Lue on 2016/7/18.
 */
public class LiveFragment extends Fragment
{
    private View view;
    private Activity mActivity;
    private AdvancedAutoCompleteTextView autoCompleteTextView;
    private AutoCompleteAdapter adapter;
    private ArrayList<String> mOriginalValues=new ArrayList<String>();
    private Button button_play;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        view = LayoutInflater.from(mActivity).inflate(R.layout.fragment_live, null);
        autoCompleteTextView = (AdvancedAutoCompleteTextView)view.findViewById(R.id.autocompltetextview);
        button_play = (Button)view.findViewById(R.id.button_play);


        mOriginalValues = SharedPrefsStrListUtil.getStrListValue(mActivity, "search_history");
        autoCompleteTextView.setThreshold(0);
        adapter = new AutoCompleteAdapter(mActivity, mOriginalValues, 10);
        autoCompleteTextView.setAdapter(adapter);

        button_play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String current = autoCompleteTextView.getInput();
                mOriginalValues.add(current);
                SharedPrefsStrListUtil.putStrListValue(mActivity, "search_history", mOriginalValues);
                Toast.makeText(mActivity, "添加"+current+"进本地", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(mActivity, PanframePlayActivity.class);
                intent.putExtra("path", "http://180.153.55.2:80/TESTING/20160824/11/01/m3u8/ctc_1-7418de6897.m3u8");
                //注意titles比path多一个
                intent.putExtra("title", "测试直播流");
                intent.putExtra("ratio", 0.5625);
                intent.putExtra("playing_mode", 1);
                startActivity(intent);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return view;
    }


}
