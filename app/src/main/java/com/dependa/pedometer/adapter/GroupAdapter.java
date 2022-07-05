package com.dependa.pedometer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.dependa.pedometer.R;
import com.dependa.pedometer.model.GroupModel;

import java.util.ArrayList;

public class GroupAdapter  extends ArrayAdapter<GroupModel> {
    private ArrayList<GroupModel> dataSet;
    Context mContext;

    private View vi;
    private ViewHolder viewHolder;

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public GroupModel getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        CheckBox cbxGroup;
    }

    public GroupAdapter(@NonNull Context context, ArrayList<GroupModel> data) {
        super(context, R.layout.item_group, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        vi = view;
        //Populate the Listview
        final int pos = position;
        GroupModel items = dataSet.get(pos);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.item_group, null);
            viewHolder = new ViewHolder();
            viewHolder.cbxGroup = (CheckBox) vi.findViewById(R.id.group);

            viewHolder.cbxGroup
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            GroupModel data = dataSet.get(pos);
                            data.setActive(isChecked);
                            dataSet.set(pos, data);
                        }
                    });

            vi.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) view.getTag();

        viewHolder.cbxGroup.setText(items.getName());
        if (items.getActive()) {
            viewHolder.cbxGroup.setChecked(true);
        } else {
            viewHolder.cbxGroup.setChecked(false);
        }
        return vi;
    }

    public ArrayList<GroupModel> getAllData() {
        return dataSet;
    }
}
