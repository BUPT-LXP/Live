package com.lue.live.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lue.live.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lue on 2016/7/18.
 */
public class LocalGridAdapter extends BaseAdapter
{
    private List<Bitmap> coverages = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private LayoutInflater listContainer;

    public LocalGridAdapter(Context context, List<Bitmap> coverages,  List<String> titles)
    {
        this.coverages = coverages;
        this.titles = titles;
        listContainer = LayoutInflater.from(context);
    }



    @Override
    public int getCount()
    {
        return titles.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //自定义视图
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = listContainer.inflate(R.layout.gridview_live, null);

            viewHolder.image_live = (ImageView)convertView.findViewById(R.id.image_live);
            viewHolder.text_live = (TextView)convertView.findViewById(R.id.text_live);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.image_live.setImageBitmap(coverages.get(position));
        viewHolder.text_live.setText(titles.get(position));


        return convertView;
    }


    class ViewHolder
    {
        ImageView image_live;
        TextView text_live;
    }
}
