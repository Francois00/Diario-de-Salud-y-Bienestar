package com.example.diariodesaludybienestar;

import android.content.Context;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class NutritionAPI {
    private static final String BASE_URL = "https://trackapi.nutritionix.com/v2/";
    private static final String APP_ID = "4af60056";
    private static final String APP_KEY = "e869076183940a714d31c7c075b16303";
    private final RequestQueue requestQueue;

    public NutritionAPI(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface NutritionCallback {
        void onSuccess(int calories, int protein);
        void onError(String errorMessage);
    }

    public void getNutritionData(String foodQuery, NutritionCallback callback) {
        String url = BASE_URL + "natural/nutrients";
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("query", foodQuery);
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> handleResponse(response, callback),
                error -> callback.onError(error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-app-id", APP_ID);
                headers.put("x-app-key", APP_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void handleResponse(JSONObject response, NutritionCallback callback) {
        try {
            JSONObject food = response.getJSONArray("foods").getJSONObject(0);
            int calories = food.getInt("nf_calories");
            int protein = food.getInt("nf_protein");
            callback.onSuccess(calories, protein);
        } catch (JSONException e) {
            callback.onError("Error processing data");
        }
    }
}