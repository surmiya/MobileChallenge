package paytm.com.currencyconverter.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import paytm.com.currencyconverter.R;

/**
 * Created by Mac-Retina on 2017-06-18.
 */

public class GridViewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    Map<String, Double> map;
    final List<String> list;
    private String amount;

    public GridViewAdapter(Context context, Map<String, Double> map, String amount) {
        this.context = context;
        inflater = (LayoutInflater.from(context));
        this.map = map;
        list = new ArrayList<String>(map.keySet());
        this.amount = amount;

    }

    @Override
    public int getCount() {
        return map.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        view = inflater.inflate(R.layout.layout_gridview_item, null); // inflate the layout
        TextView labelTextView = (TextView) view.findViewById(R.id.currency_label);
        TextView rateTextView = (TextView) view.findViewById(R.id.currency_value);

        String label = list.get(position);
        labelTextView.setText(label);

        double exValue = map.get(label);
        NumberFormat nf = NumberFormat.getInstance();

        if(!TextUtils.isEmpty(amount)) {
            try {
                exValue = nf.parse(amount).doubleValue() * (map.get(label));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        DecimalFormat df = new DecimalFormat("#,##0.000");
        rateTextView.setText(String.valueOf(df.format(exValue)));

        view.setEnabled(false);

        return view;
    }
}
