package main.java.model.microsoft.api;

import static main.java.constants.Constants.API_KEY1;
import static main.java.constants.Constants.API_REGION;

import java.io.File;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

/**
 * This class contains the methods about the faces.
 */
public class Face_API {

  private static String personGroupId = "default-group";

  /**
   * Detect human faces in an image and returns face locations, and optionally
   * with faceIds, landmarks, and attributes. Optional parameters for returning
   * faceId, landmarks, and attributes. Attributes include age, gender, smile
   * intensity, facial hair, head pose, glasses, emotion, hair, makeup,
   * occlusion, accessories, blur, exposure and noise. faceId is for other APIs
   * use including Face - Identify, Face - Verify, and Face - Find Similar. The
   * faceId will expire 24 hours after detection call.
   *
   * @param imagePath
   *          - The path of the image to detect
   * @return A successful call returns an array of face entries ranked by face
   *         rectangle size in descending order. An empty response indicates no
   *         faces detected.
   */
  public static JSONArray detectFaces(String imagePath) {

    JSONArray jsonArray = null;

    CloseableHttpClient httpclient = HttpClients.createDefault();

    try {

      URIBuilder builder = new URIBuilder(API_REGION + "/detect");

      // Request parameters. All of them are optional.
      builder.setParameter("returnFaceId", "true");
      builder.setParameter("returnFaceLandmarks", "false");
      builder.setParameter("returnFaceAttributes",
          "age,gender,smile,glasses,exposure");

      // Prepare the URI for the REST API call.
      URI uri = builder.build();
      HttpPost request = new HttpPost(uri);

      // Request headers.
      request.setHeader("Content-Type", "application/octet-stream");
      request.setHeader("Ocp-Apim-Subscription-Key", API_KEY1);

      // Request body.
      File file = new File(imagePath);
      FileEntity reqEntity = new FileEntity(file,
          ContentType.APPLICATION_OCTET_STREAM);
      request.setEntity(reqEntity);

      // Execute the REST API call and get the response entity.
      HttpResponse response = httpclient.execute(request);
      HttpEntity entity = response.getEntity();

      // If not null, format and display the JSON response.
      if (entity != null)
        jsonArray = new JSONArray(EntityUtils.toString(entity).trim());
    } catch (Exception e) {
      // Display error message.
      System.out.println(e.getMessage());
    }

    return jsonArray;
  }

  /**
   * Identify unknown faces from a person group. For each face in the faceIds
   * array, Face Identify will compute similarities between the query face and
   * all the faces in the person group (given by personGroupId), and returns
   * candidate person(s) for that face ranked by similarity confidence. The
   * person group should be trained to make it ready for identification.
   *
   * @param faceIds
   *          - Array of query faces faceIds, created by the Face - Detect. Each
   *          of the faces are identified independently. The valid number of
   *          faceIds is between [1, 10]
   * @param maxNumOfCandidatesReturned
   *          - The range of maxNumOfCandidatesReturned is between 1 and 5
   *          (default is 1)
   * @param confidenceThreshold
   *          - Confidence threshold of identification, used to judge whether
   *          one face belong to one person. The range of confidenceThreshold is
   *          [0, 1]
   * @return A successful call returns the identified candidate person(s) for
   *         each query face.
   */
  public static JSONArray identifyFaces(String[] faceIds,
      int maxNumOfCandidatesReturned, double confidenceThreshold) {

    JSONArray jsonArray = null;

    HttpClient httpclient = HttpClients.createDefault();

    try {
      URIBuilder builder = new URIBuilder(API_REGION + "/identify");

      // Prepare the URI for the REST API call.
      URI uri = builder.build();
      HttpPost request = new HttpPost(uri);

      // Request headers.
      request.setHeader("Content-Type", "application/json");
      request.setHeader("Ocp-Apim-Subscription-Key", API_KEY1);

      String faceIdsString = "[";
      for (int i = 0; i < faceIds.length; i++) {
        faceIdsString += "\"" + faceIds[i] + "\"";
        if (i + 1 < faceIds.length)
          faceIdsString += ",";
      }
      faceIdsString += "]";

      String body = "{ " + "\"personGroupId\":\"" + personGroupId + "\","
          + "\"faceIds\":" + faceIdsString + ","
          + "\"maxNumOfCandidatesReturned\":" + maxNumOfCandidatesReturned + ","
          + "\"confidenceThreshold\":" + confidenceThreshold + "}";

      // Request body
      StringEntity reqEntity = new StringEntity(body);
      request.setEntity(reqEntity);

      // Execute the REST API call and get the response entity.
      HttpResponse response = httpclient.execute(request);
      HttpEntity entity = response.getEntity();

      // If not null, format and display the JSON response.
      if (entity != null)
        jsonArray = new JSONArray(EntityUtils.toString(entity).trim());
    } catch (Exception e) {
      // Display error message.
      System.out.println(e.getMessage());
    }

    return jsonArray;
  }

}
