package paytm.com.currencyconverter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import paytm.com.currencyconverter.model.FxResponse;
import paytm.com.currencyconverter.remote.FxAPI;
import paytm.com.currencyconverter.views.CurrencyAdapter;
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
        createFxAPI();

        fxAPI.getFxRates(base).enqueue(callback);
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

                SharedPreferences settings = getApplicationContext().getSharedPreferences("fx_rates", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
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
                fxAPI.getFxRates(spinnerSelection).enqueue(callback);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadGrid(FxResponse fxResponse) {
        if(spinnerSelection.equals(fxResponse.getBaseCurr())) {
            gridview.setAdapter(new CurrencyAdapter(MainActivity.this, fxResponse.getRates()));
        }
    }

}


