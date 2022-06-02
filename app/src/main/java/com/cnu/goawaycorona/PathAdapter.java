package com.cnu.goawaycorona;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PathAdapter extends BaseAdapter {
    private static final String TAG = "PathAdapter";
    private final Activity activity;
    private final ArrayList<ItemData> arrayList = new ArrayList<>();

    public PathAdapter(Activity activity) {
        this.activity = activity;
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @NonNull

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.i(TAG,"getView called");
        if(convertView==null) {
            Log.i(TAG,"getView: convertView==null");
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.layout_item, parent,false);
        }
// convertView is a LinearLayout who root node of xml.
        LinearLayout linearLayout = (LinearLayout)convertView;

// resize height of linear layout
//        Display display = activity.getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//
//        int itemHeight = size.x/5;
//        ViewGroup.LayoutParams param = linearLayout.getLayoutParams();
//        param.height = itemHeight;
//        linearLayout.setLayoutParams(param);

// set item data on the view
        ItemData itemData = arrayList.get(position);

        TextView textViewDateTime = linearLayout.findViewById(R.id.textViewDateTime);
        TextView textViewAddress = linearLayout.findViewById(R.id.textViewAddress);
        TextView textViewStatus = linearLayout.findViewById(R.id.textViewStatus);

        textViewDateTime.setText(itemData.getTime());
        textViewAddress.setText(itemData.getAns());

        if(itemData.isStatus()) {
            textViewStatus.setText("정상");
            textViewDateTime.setBackgroundColor(Color.argb(255,255,255,255));
            textViewAddress.setBackgroundColor(Color.argb(255,255,255,255));
            textViewStatus.setBackgroundColor(Color.argb(255,0,196,0));
        }
        else {
            // 수정한 주소가 있으면 그걸로 표시한다.
            String address = itemData.getAddress();
            if(address==null) {
                textViewStatus.setText("수정필요");
                textViewDateTime.setBackgroundColor(Color.argb(255,255,196,196));
                textViewAddress.setBackgroundColor(Color.argb(255,255,196,196));
                textViewStatus.setBackgroundColor(Color.argb(255,196,0,0));
                textViewAddress.setText("없음");
            }
            else {
                // 만약 주소가 맞으면 올바르게 표시한다.
                if(address.equals(itemData.getAns())) {
                    textViewStatus.setText("정상");
                    textViewDateTime.setBackgroundColor(Color.argb(255,255,255,255));
                    textViewAddress.setBackgroundColor(Color.argb(255,255,255,255));
                    textViewStatus.setBackgroundColor(Color.argb(255,0,196,0));
                    textViewAddress.setText(itemData.getAns());
                }
                else {
                    // 주소가 다르면 주소는 표시하되 비정상으로 표시한다.
                    textViewStatus.setText("수정필요");
                    textViewDateTime.setBackgroundColor(Color.argb(255,255,196,196));
                    textViewAddress.setBackgroundColor(Color.argb(255,255,196,196));
                    textViewStatus.setBackgroundColor(Color.argb(255,196,0,0));
                    textViewAddress.setText(address);
                }


            }
        }

        return linearLayout;
    }

    public void addItem(ItemData item) {
        arrayList.add(item);
        System.out.println("item added:"+item);
    }


    public void removeAllItems() {
        arrayList.clear();
    }

    public void setAddress(int index, String address) {
        ItemData itemData = arrayList.get(index);
        itemData.setAddress(address);
        arrayList.set(index,itemData);
    }
}
