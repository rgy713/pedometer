package com.dependa.pedometer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dependa.pedometer.adapter.GroupAdapter;
import com.dependa.pedometer.base.Constants;
import com.dependa.pedometer.base.HttpPostRequest;
import com.dependa.pedometer.base.StepBase;
import com.dependa.pedometer.model.GroupModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupActivity extends StepBase {
    private UpdateGroupTask mUpdateGroupTask = null;

    private JSONArray detailData = null;
    private ListView listView;
    GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_title);
        title.setText(R.string.group_setting);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        try {
            detailData = new JSONArray(bundle.getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }

        ArrayList<GroupModel> mList = new ArrayList<GroupModel>();
        for (int i = 0; i < detailData.length(); i++) {
            JSONObject one = null;
            try {
                one = detailData.getJSONObject(i);
                GroupModel info = new GroupModel(one.getInt("id"), one.getString("name"), one.getInt("active") > 0);
                mList.add(info);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        listView = (ListView) findViewById(R.id.list_view);
        groupAdapter = new GroupAdapter(this, mList);
        listView.setAdapter(groupAdapter);

        Button applyBtn = (Button) findViewById(R.id.apply_btn);

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<GroupModel> data = groupAdapter.getAllData();
                updateGroup(data);
            }
        });
    }

    private void updateGroup(ArrayList<GroupModel> groupModel) {
        if (mUpdateGroupTask != null) {
            return;
        }

        SharedPreferences sp1 = getSharedPreferences(Constants.SHARE_PREF, 0);
        String email = sp1.getString(Constants.SHARE_EMAIL, null);

        mUpdateGroupTask = new UpdateGroupTask(email, groupModel);
        mUpdateGroupTask.execute((Void) null);
    }

    private class UpdateGroupTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final ArrayList<GroupModel> mGroupModel;

        private String mErrorMsg;

        UpdateGroupTask(String mEmail, ArrayList<GroupModel> groupModel) {
            this.mEmail = mEmail;
            this.mGroupModel = groupModel;
        }

        private boolean isConnected() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isConnected()) {

                ContentValues postParams = new ContentValues();
                JSONObject result = null;
                try {
                    postParams.put("email", mEmail);

                    for (GroupModel one : mGroupModel) {
                        postParams.put("group[" + one.getId() + "]", one.getActive() ? 1 : 0);
                    }

                    HttpPostRequest httpPostRequest = new HttpPostRequest();

                    result = httpPostRequest.POST(Constants.SET_GROUP_LIST, postParams);

                    if (result == null) return false;
                    if (!result.getString("type").equals("success")) {
                        mErrorMsg = result.getString("content");
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                mErrorMsg = Constants.CONNECT_INTERNET;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdateGroupTask = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
            Integer message;
            if (success) {
                message = R.string.save_success;
            } else {
                switch (mErrorMsg) {
                    case Constants.CONNECT_INTERNET:
                        message = R.string.connet_internet;
                        break;
                    default:
                        message = R.string.save_error;
                }
            }
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        @Override
        protected void onCancelled() {
            mUpdateGroupTask = null;
        }
    }
}
