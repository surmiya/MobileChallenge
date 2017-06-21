package paytm.com.currencyconverter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import paytm.com.currencyconverter.model.FxResponse;
import paytm.com.currencyconverter.remote.FxAPI;
import paytm.com.currencyconverter.views.GridViewAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mac-Retina on 2017-06-18.
 */

public class MainActivity extends AppCompatActivity {

    private FxAPI fxAPI;
    private Spinner currencySpinner;
    private GridView gridview;
    private EditText amountEditText;

    private boolean isSpinnerLoaded = false;
    private String spinnerSelection;

    //30 minutes in milliseconds
    private final int updateFrequency = 1800000;
    private String base = "CAD";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currencySpinner = (Spinner) findViewById(R.id.spinner2);
        gridview = (GridView) findViewById(R.id.gridview);
        amountEditText = (EditText) findViewById(R.id.amount) ;
        amountEditText.addTextChangedListener(new AmountTextWatcher());

        createFxAPI();
        fxAPI.getFxRates(base).enqueue(callback);

        amountEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    updateResults();
                    return true;
                }
                return false;
            }
        });
    }

    private void createFxAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FxAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        fxAPI = retrofit.create(FxAPI.class);
    }

    Callback<JsonObject> callback = new Callback<JsonObject>() {

        @Override
        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
            if (response.isSuccessful()) {
                Gson gson = new Gson();
                FxResponse fxResponse = gson.fromJson(response.body().toString(), FxResponse.class);

                SharedPreferences prefs = MainActivity.this.getSharedPreferences("fx_rates", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(fxResponse.getBaseCurr(), response.body().toString());
                editor.putLong(fxResponse.getBaseCurr() + "_expiry_timestamp", System.currentTimeMillis() + updateFrequency);
                editor.commit();

                if(!isSpinnerLoaded) {
                    loadSpinner(fxResponse);
                } else {
                    loadGrid(fxResponse);
                }
            }
        }

        @Override
        public void onFailure(Call<JsonObject> call, Throwable t) {
            t.printStackTrace();

        }
    };

    private void loadSpinner(FxResponse fxResponse) {
        final List<String> list = new ArrayList<String>(fxResponse.getRates().keySet());
        list.add(fxResponse.getBaseCurr());
        Collections.sort(list);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(spinnerAdapter);
        isSpinnerLoaded = true;

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                spinnerSelection = list.get(position);

                updateResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadGrid(FxResponse fxResponse) {
        if(spinnerSelection.equals(fxResponse.getBaseCurr())) {
            gridview.setAdapter(new GridViewAdapter(MainActivity.this, fxResponse.getRates(), amountEditText.getText().toString()));
        }
    }

    private boolean updateGridViewFromSharedPref() {

        SharedPreferences prefs = MainActivity.this.getSharedPreferences("fx_rates", MODE_PRIVATE);
        String jsonString = prefs.getString(spinnerSelection, null);
        long expiry = prefs.getLong(spinnerSelection + "_expiry_timestamp", -1);

        if(-1 != expiry && expiry > System.currentTimeMillis() && null != jsonString ) {
            Gson gson = new Gson();
            FxResponse fxResponse = gson.fromJson(jsonString, FxResponse.class);
            fxResponse.setBaseCurr(spinnerSelection);
            loadGrid(fxResponse);
            return true;
        }

        return false;
    }

    private void updateResults() {

        // If Exists in shared Pref and not expired - load from SharedPref
        // Else make an api call
        if(!updateGridViewFromSharedPref()) {
            fxAPI.getFxRates(spinnerSelection).enqueue(callback);
        }
    }

    public class AmountTextWatcher implements TextWatcher {

        private final DecimalFormat decimalFormat;
        private String current = "";


        public AmountTextWatcher() {
            decimalFormat = new DecimalFormat("#.00");
            decimalFormat.setDecimalSeparatorAlwaysShown(true);
        }

        @Override
        public void afterTextChanged(Editable s) {
            amountEditText.removeTextChangedListener(this);

            if (s != null && !s.toString().isEmpty() &&  !s.toString().equals(current)  ) {
                String cleanString = s.toString().replaceAll("[$,.]", "");

                double parsed = Double.parseDouble(cleanString);
                String formatted = NumberFormat.getCurrencyInstance().format((parsed/100));

                current = formatted;
                amountEditText.setText(formatted.substring(1,formatted.length()));
                amountEditText.setSelection(formatted.length()-1);
            }

            amountEditText.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

}


