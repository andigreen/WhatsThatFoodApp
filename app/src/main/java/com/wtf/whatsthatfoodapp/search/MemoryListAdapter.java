package com.wtf.whatsthatfoodapp.search;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

import java.util.List;

/**
 * Created by andig on 3/8/2017.
 */

public class MemoryListAdapter extends BaseAdapter{

    private Context mContext;
    private MemoryDao dao;

    //Constructor
    public MemoryListAdapter(Context mContext, MemoryDao dao) {
        this.mContext = mContext;
        this.dao = dao;
    }

    @Override
    public int getCount() {
        return 0;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
