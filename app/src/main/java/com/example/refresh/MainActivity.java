package com.example.refresh;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> data;
    private RefreshListview listView;
    private myAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("rrrr");

        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题


        setContentView(R.layout.activity_main);

        listView = (RefreshListview) findViewById(R.id.list_item);

        //gei刷新listview设置监听 自定义接口

        listView.setRefreshListenner(new RefreshListview.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Thread(){
                    @Override
                    public void run() {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        data.add(0,"我是刷新出来的数据");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                listView.hide();
                            }
                        });

//                        listView.post(new Runnable() {
//                            @Override


//                            public void run() {
//                                adapter.notifyDataSetChanged();
//                                listView.hide();
//                            }
//                        });

                    }
                }.start();

            }

            @Override
            public void loadMore() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        data.add("afafafaf");
                        data.add("afafafaf");

                        data.add("afafafaf");


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                listView.hide();
                            }
                        });
                    }
                }).start();

            }

        });

        data = new ArrayList<>();

        for (int i = 0; i < 30; i++) {

            data.add(""+i);

        }


        adapter = new myAdapter();
        listView.setAdapter(adapter);


    }

    private class myAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=View.inflate(MainActivity.this,R.layout.item_list,null);
            }

            TextView tv= (TextView) convertView.findViewById(R.id.tv);

            tv.setText(data.get(position));

            return convertView;
        }
    }
}
