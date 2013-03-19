package com.meltmedia.cadmium.blackbox.test.endpoints;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.meltmedia.cadmium.blackbox.test.AbstractBodyApiResponseValidator;
import com.meltmedia.cadmium.blackbox.test.ApiRequest;
import com.meltmedia.cadmium.core.history.HistoryEntry;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Endpoint test for system history.
 *
 * @author John McEntire
 */
public class HistoryLimitEndpointTest extends AbstractEnpointTest {
  public HistoryLimitEndpointTest(String token) {
    super(token);
  }

  @Override
  public void setupTest() {
    request = new ApiRequest("http://localhost:8901/system/history?limit=1", ApiRequest.Method.GET, authToken);
    validator = new AbstractBodyApiResponseValidator("application/json", HttpStatus.SC_OK) {
      @Override
      public void validateBody(HttpResponse response, String responseBody) throws Exception {
        List<HistoryEntry> history = null;
        try {
          Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

            @Override
            public Date deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext ctx) throws JsonParseException {
              return new Date(json.getAsLong());
            }

          }).create();
          history = gson.fromJson(responseBody, new TypeToken<List<HistoryEntry>>() {
          }.getType());
        } catch(Exception e) {
          fail("Failed to parse history response {"+responseBody+"}: " + e.getMessage());
        }
        assertNotNull("History did not parse", history);
        assertTrue("No history entries returned", history.size() == 1);
      }
    };
  }
}
