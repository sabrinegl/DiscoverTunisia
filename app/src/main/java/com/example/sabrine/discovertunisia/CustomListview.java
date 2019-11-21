package com.example.sabrine.discovertunisia;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListview extends ArrayAdapter<String> {
   private String[] fruitname=null;
   //private String[] desc=null;
   private Integer[] imgrid=null;
   private Activity context=null;
    public CustomListview(Activity context, String[] fruitname, Integer[] imgrid) {
        super(context, R.layout.listview_layout,fruitname);
        this.context=context;
        this.fruitname=fruitname;
        //this.desc=desc;
        this.imgrid=imgrid;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View r=convertView;
        ViewHolder viewHolder=null;
        if (r==null){
            LayoutInflater layoutInflater=context.getLayoutInflater();
            r=layoutInflater.inflate(R.layout.listview_layout,null,true);
            viewHolder =new ViewHolder(r);
            r.setTag(viewHolder);

        }
        else {
            viewHolder=(ViewHolder)r.getTag();
        }


        viewHolder.ivw.setImageResource(imgrid[position]);
        viewHolder.tvw1.setText(fruitname[position]);
        //viewHolder.tvw2.setText(desc[position]);




        return r;
    }
    class ViewHolder{
        TextView tvw1;
        //TextView tvw2;
        ImageView ivw;
        ViewHolder(View v){
            tvw1= (TextView)v.findViewById(R.id.tvfruitname);
           // tvw1= (TextView)v.findViewById(R.id.tvdescription);
            ivw= (ImageView) v.findViewById(R.id.imageView3);


        }
    }
}
