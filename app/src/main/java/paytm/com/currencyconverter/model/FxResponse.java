package paytm.com.currencyconverter.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by Mac-Retina on 2017-06-18.
 */

public class FxResponse {

    @SerializedName("base")
    String baseCurr;

    @SerializedName("date")
    String date;

    Map<String, Double> rates;

    public String getBaseCurr() {
        return baseCurr;
    }

    public void setBaseCurr(String baseCurr) {
        this.baseCurr = baseCurr;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

}
