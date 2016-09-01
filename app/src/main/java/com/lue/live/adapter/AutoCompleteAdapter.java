package com.lue.live.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lue.live.R;
import com.lue.live.model.SharedPrefsStrListUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lue on 2016/8/22.
 */
public class AutoCompleteAdapter extends BaseAdapter implements Filterable
{
    private Context context;
    private ArrayFilter mFilter;
    private ArrayList<String> mOriginalValues;//���е�Item
    private List<String> mObjects;//���˺��item
    private final Object mLock = new Object();
    private int maxMatch=10;//�����ʾ���ٸ�ѡ��,������ʾȫ��
    public AutoCompleteAdapter(Context context,ArrayList<String> mOriginalValues,int maxMatch){
        this.context=context;
        this.mOriginalValues=mOriginalValues;
        this.maxMatch=maxMatch;
    }

    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            // TODO Auto-generated method stub
            FilterResults results = new FilterResults();

//			if (mOriginalValues == null) {
//                synchronized (mLock) {
//                    mOriginalValues = new ArrayList<String>(mObjects);//
//                }
//            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (mLock) {
                    Log.i("tag", "mOriginalValues.size="+mOriginalValues.size());
                    ArrayList<String> list = new ArrayList<String>(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                    return results;
                }
            } else {
                String prefixString = prefix.toString().toLowerCase();

                final int count = mOriginalValues.size();

                final ArrayList<String> newValues = new ArrayList<String>(count);

                for (int i = 0; i < count; i++) {
                    final String value = mOriginalValues.get(i);
                    final String valueText = value.toLowerCase();

//                    if(valueText.contains(prefixString)){//ƥ������
//
//                    }
                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {  //Դ�� ,ƥ�俪ͷ
                        newValues.add(value);
                    }
//                    else {
//                        final String[] words = valueText.split(" ");//�ָ���ƥ�䣬Ч�ʵ�
//                        final int wordCount = words.length;
//
//                        for (int k = 0; k < wordCount; k++) {
//                            if (words[k].startsWith(prefixString)) {
//                                newValues.add(value);
//                                break;
//                            }
//                        }
//                    }
                    if(maxMatch>0){//����������
                        if(newValues.size()>maxMatch-1){//��Ҫ̫��
                            break;
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // TODO Auto-generated method stub
            mObjects = (List<String>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mObjects.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        //�˷������󣬾�����Ҫʹ��
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder = null;
        if(convertView==null){
            holder=new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.simple_list_item_for_autocomplete, null);
            holder.tv=(TextView)convertView.findViewById(R.id.simple_item_0);
            holder.iv=(ImageView)convertView.findViewById(R.id.simple_item_1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv.setText(mObjects.get(position));
        holder.iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String obj=mObjects.remove(position);
                mOriginalValues.remove(obj);
                notifyDataSetChanged();
                SharedPrefsStrListUtil.putStrListValue(context,"search_history",mOriginalValues);
                Toast.makeText(context, "删除"+obj, Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView tv;
        ImageView iv;
    }

    public ArrayList<String> getAllItems(){
        return mOriginalValues;
    }
}