package com.example.WiFiPasswordSearcher;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;


public class MyAdapterWps extends BaseAdapter {

	ArrayList<ItemWps> data = new ArrayList<ItemWps>();
	Context context;

	public MyAdapterWps(Context context, ArrayList<ItemWps> arr) {
		if (arr != null) {
			data = arr;
		}
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int num) {
		// TODO Auto-generated method stub
		return data.get(num);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int i, View someView, ViewGroup arg2) {
		//Получение объекта inflater из контекста
		LayoutInflater inflater = LayoutInflater.from(context);
		//Если someView (View из ListView) вдруг оказался равен
		//null тогда мы загружаем его с помошью inflater 
		if (someView == null) {
			someView = inflater.inflate(R.layout.list_wps_item, arg2, false);
		}
		//Обявляем наши текствьюшки и связываем их с разметкой
		TextView pin = (TextView) someView.findViewById(R.id.txtPin);
		TextView metod = (TextView) someView.findViewById(R.id.txtMetod);
		TextView score = (TextView) someView.findViewById(R.id.txtScor);
		TextView db = (TextView) someView.findViewById(R.id.txtDb);
		// 
		pin.setText(data.get(i).pin);
		metod.setText(data.get(i).metod);
		score.setText(data.get(i).score);
		db.setText(data.get(i).db);
		return someView;
	}

}
