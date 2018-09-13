package main.java.model.microsoft.api;

import static main.java.constants.Constants.API_KEY1;
import static main.java.constants.Constants.API_REGION;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * This class contains the methods about the persons group.
 */
public class PersonGroup_API {

  private static String faceListId    = "default-list";
  private static String personGroupId = "default-group";

  /**
   * Queue a person group training task, the training task may not be started
   * immediately.
   *
   * @return A successful call returns an empty JSON body.
   */
  public static JSONObject trainPersonGroup() {

    JSONObject jsonObject = null;

    HttpClient httpclient = HttpClients.createDefault();

    try {
      URIBuilder builder = new URIBuilder(
          API_REGION + "/persongroups/" + personGroupId + "/train");

      URI uri = builder.build();
      HttpPost request = new HttpPost(uri);
      request.setHeader("Ocp-Apim-Subscription-Key", API_KEY1);

      HttpResponse response = httpclient.execute(request);
      HttpEntity entity = response.getEntity();

      // If not null, format and display the JSON response.
      if (entity != null)
        jsonObject = new JSONObject(EntityUtils.toString(entity).trim());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return jsonObject;
  }

}
