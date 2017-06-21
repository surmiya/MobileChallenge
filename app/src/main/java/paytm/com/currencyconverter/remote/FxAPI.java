package paytm.com.currencyconverter.remote;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import paytm.com.currencyconverter.model.FxResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Mac-Retina on 2017-06-18.
 */

public interface FxAPI {

    static final String BASE_URL = "http://api.fixer.io/";

    @GET("latest")
    Call<JsonObject> getFxRates(@Query("base") String base);

}
