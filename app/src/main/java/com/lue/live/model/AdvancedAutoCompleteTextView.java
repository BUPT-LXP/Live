package com.lue.live.model;

/**
 * Created by Lue on 2016/8/22.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lue.live.R;
import com.lue.live.adapter.AutoCompleteAdapter;

public class AdvancedAutoCompleteTextView extends RelativeLayout{

    private Context context;
    private AutoCompleteTextView tv;
    public AdvancedAutoCompleteTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context=context;
    }
    public AdvancedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.context=context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }

    private void initViews() {
        LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        tv=new AutoCompleteTextView(context);
        tv.setLayoutParams(params);
        tv.setSingleLine(true);
        tv.setHint("请输入直播地址");
        tv.setBackground(context.getResources().getDrawable(R.drawable.shape_autocompletetextview));



        LayoutParams p=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p.addRule(RelativeLayout.CENTER_VERTICAL);
        p.rightMargin=10;
        ImageView iv=new ImageView(context);
        iv.setLayoutParams(p);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setImageResource(R.mipmap.delete);
        iv.setClickable(true);
        iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                tv.setText("");
            }
        });

        this.addView(tv);
        this.addView(iv);
    }

    public void setAdapter(AutoCompleteAdapter adapter){
        tv.setAdapter(adapter);
    }

    public void setThreshold(int threshold){
        tv.setThreshold(threshold);
    }

    public AutoCompleteTextView getAutoCompleteTextView(){
        return tv;
    }

    public String getInput()
    {
        return tv.getText().toString();
    }
}
