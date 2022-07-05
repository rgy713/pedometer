package com.dependa.pedometer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.dependa.pedometer.R;
import com.dependa.pedometer.model.MealInfoModel;

import java.util.ArrayList;

public class MealInfoAdapter extends ArrayAdapter<MealInfoModel> {
    private ArrayList<MealInfoModel> dataSet;
    Context mContext;

    private View vi;
    private ViewHolder viewHolder;

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public MealInfoModel getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        TextView txtFoodName;
        CheckBox cbxBreafast;
        CheckBox cbxLunch;
        CheckBox cbxDinner;
    }

    public MealInfoAdapter(@NonNull Context context, ArrayList<MealInfoModel> data) {
        super(context, R.layout.item_food_info, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        vi = view;
        //Populate the Listview
        final int pos = position;
        MealInfoModel items = dataSet.get(pos);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.item_food_info, null);
            viewHolder = new ViewHolder();
            viewHolder.txtFoodName = (TextView) vi.findViewById(R.id.food_name);
            viewHolder.cbxBreafast = (CheckBox) vi.findViewById(R.id.breakfast);
            viewHolder.cbxLunch = (CheckBox) vi.findViewById(R.id.lunch);
            viewHolder.cbxDinner = (CheckBox) vi.findViewById(R.id.dinner);

            viewHolder.cbxBreafast
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            MealInfoModel data = dataSet.get(pos);
                            data.setBreakfast(isChecked);
                            dataSet.set(pos, data);
                        }
                    });

            viewHolder.cbxLunch
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            MealInfoModel data = dataSet.get(pos);
                            data.setLunch(isChecked);
                            dataSet.set(pos, data);
                        }
                    });

            viewHolder.cbxDinner
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            MealInfoModel data = dataSet.get(pos);
                            data.setDinner(isChecked);
                            dataSet.set(pos, data);
                        }
                    });

            vi.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) view.getTag();

        viewHolder.txtFoodName.setText(items.getFoodName());
        if (items.getBreakfast()) {
            viewHolder.cbxBreafast.setChecked(true);
        } else {
            viewHolder.cbxBreafast.setChecked(false);
        }
        if (items.getLunch()) {
            viewHolder.cbxLunch.setChecked(true);
        } else {
            viewHolder.cbxLunch.setChecked(false);
        }
        if (items.getDinner()) {
            viewHolder.cbxDinner.setChecked(true);
        } else {
            viewHolder.cbxDinner.setChecked(false);
        }

        return vi;
    }

    public ArrayList<MealInfoModel> getAllData() {
        return dataSet;
    }
}
