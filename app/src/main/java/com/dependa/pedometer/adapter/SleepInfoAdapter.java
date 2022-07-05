package com.dependa.pedometer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dependa.pedometer.R;
import com.dependa.pedometer.model.MealInfoModel;
import com.dependa.pedometer.model.SleepInfoModel;

import java.util.ArrayList;

public class SleepInfoAdapter extends ArrayAdapter<SleepInfoModel> {
    private ArrayList<SleepInfoModel> dataSet;
    Context mContext;

    private View vi;
    private SleepInfoAdapter.ViewHolder viewHolder;

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public SleepInfoModel getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView txtSqName;
        RadioGroup rgLevel;
        RadioButton rbLevel0;
        RadioButton rbLevel1;
        RadioButton rbLevel2;
        RadioButton rbLevel3;
    }

    public SleepInfoAdapter(@NonNull Context context, ArrayList<SleepInfoModel> data) {
        super(context, R.layout.item_sleep_info, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        vi = view;
        //Populate the Listview
        final int pos = position;
        SleepInfoModel items = dataSet.get(pos);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.item_sleep_info, null);
            viewHolder = new SleepInfoAdapter.ViewHolder();
            viewHolder.txtSqName = (TextView) vi.findViewById(R.id.sq_name);
            viewHolder.rgLevel = (RadioGroup) vi.findViewById(R.id.sq_level);
            viewHolder.rbLevel0 = (RadioButton) vi.findViewById(R.id.level0);
            viewHolder.rbLevel1 = (RadioButton) vi.findViewById(R.id.level1);
            viewHolder.rbLevel2 = (RadioButton) vi.findViewById(R.id.level2);
            viewHolder.rbLevel3 = (RadioButton) vi.findViewById(R.id.level3);

            viewHolder.rgLevel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    Integer value = 3;
                    switch (checkedId) {
                        case R.id.level0:
                            value = 0;
                            break;
                        case R.id.level1:
                            value = 1;
                            break;
                        case R.id.level2:
                            value = 2;
                            break;
                        case R.id.level3:
                            value = 3;
                            break;
                    }

                    SleepInfoModel data = dataSet.get(pos);
                    data.setLevel(value);
                    dataSet.set(pos, data);
                }
            });

            vi.setTag(viewHolder);
        } else
            viewHolder = (SleepInfoAdapter.ViewHolder) view.getTag();

        viewHolder.txtSqName.setText(items.getSqName());
        viewHolder.rbLevel0.setText(items.getLevel0());
        viewHolder.rbLevel1.setText(items.getLevel1());
        viewHolder.rbLevel2.setText(items.getLevel2());
        viewHolder.rbLevel3.setText(items.getLevel3());
        switch (items.getLevel()) {
            case 0:
                viewHolder.rbLevel0.setChecked(true);
                break;
            case 1:
                viewHolder.rbLevel1.setChecked(true);
                break;
            case 2:
                viewHolder.rbLevel2.setChecked(true);
                break;
            case 3:
                viewHolder.rbLevel3.setChecked(true);
                break;
        }
        return vi;
    }

    public ArrayList<SleepInfoModel> getAllData() {
        return dataSet;
    }
}
