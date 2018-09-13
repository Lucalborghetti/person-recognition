package main.java.model.microsoft.api;

import static main.java.constants.Constants.API_KEY1;
import static main.java.constants.Constants.API_REGION;
import static main.java.constants.Constants.STORAGE_CONNECTION_STRING;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

/**
 * This class contains the methods about the persons.
 */
public class Person_API {

  private static String personGroupId = "default-group";

  /**
   * Create a new person in a specified person group. A newly created person
   * have no registered face, you can call Person - Add a Person Face API to add
   * faces to the person.
   *
   * @param name
   *          - Display name of the target person. The maximum length is 128.
   * @param userData
   *          - Optional fields for user-provided data attached to a person.
   *          Size limit is 16KB.
   * @return A successful call returns a new personId created.
   */
  public static JSONObject createPerson(String name, String userData) {

    JSONObject jsonObject = null;

    HttpClient httpclient = HttpClients.createDefault();

    try {
      URIBuilder builder = new URIBuilder(
          API_REGION + "/persongroups/" + personGroupId + "/persons");

      URI uri = builder.build();
      HttpPost request = new HttpPost(uri);
      request.setHeader("Content-Type", "application/json");
      request.setHeader("Ocp-Apim-Subscription-Key", API_KEY1);

      // Request body
      String body = "{ " + "\"name\":\"" + name + "\"," + "\"userData\":\""
          + userData + "\"}";

      StringEntity reqEntity = new StringEntity(body);
      request.setEntity(reqEntity);

      HttpResponse response = httpclient.execute(request);
      HttpEntity entity = response.getEntity();

      if (entity != null)
        jsonObject = new JSONObject(EntityUtils.toString(entity).trim());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return jsonObject;
  }

  /**
   * Add a representative face to a person for identification. The input face is
   * specified as an image with a targetFace rectangle. It returns a
   * persistedFaceId representing the added face and this persistedFaceId will
   * not expire. Note persistedFaceId is different from faceId which represents
   * the detected face by Face - Detect.
   *
   * @param imagePath
   *          - Face image path. Valid image size is from 1KB to 4MB. Only one
   *          face is allowed per image.
   * @param personId
   *          - Target person that the face is added to.
   * @param userData
   *          - User-specified data about the target face to add for any
   *          purpose. The maximum length is 1KB.
   * @return A successful call returns the new persistedFaceId.
   *
   */
  public static JSONObject addPersonFace(String imagePath, String personId,
      String userData) {

    JSONObject jsonObject = null;

    HttpClient httpclient = HttpClients.createDefault();

    try {
      URIBuilder builder = new URIBuilder(API_REGION + "/persongroups/"
          + personGroupId + "/persons/" + personId + "/persistedFaces");

      builder.setParameter("userData", userData);

      URI uri = builder.build();
      HttpPost request = new HttpPost(uri);
      request.setHeader("Content-Type", "application/json");
      request.setHeader("Ocp-Apim-Subscription-Key", API_KEY1);

      // --------------------------
      // Blob connection
      // --------------------------

      // Retrieve storage account, blob and container
      CloudStorageAccount storageAccount = CloudStorageAccount
          .parse(STORAGE_CONNECTION_STRING);
      CloudBlobClient serviceClient = storageAccount.createCloudBlobClient();
      CloudBlobContainer container = serviceClient
          .getContainerReference("face-api-blobs");
      container.createIfNotExists();

      // Configure a container for public access
      BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
      containerPermissions
          .setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
      container.uploadPermissions(containerPermissions);

      // Create or overwrite the blob with contents from a local file.
      CloudBlockBlob blob = container
          .getBlockBlobReference("add_person_face.jpg");
      File source = new File(imagePath);
      blob.upload(new FileInputStream(source), source.length());

      // Request body
      String body = "{ " + "\"url\":\"" + blob.getUri() + "\"}";

      StringEntity reqEntity = new StringEntity(body);
      request.setEntity(reqEntity);

      HttpResponse response = httpclient.execute(request);
      HttpEntity entity = response.getEntity();

      if (entity != null)
        jsonObject = new JSONObject(EntityUtils.toString(entity).trim());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return jsonObject;
  }

  /**
   * Retrieve a person's information, including registered persisted faces, name
   * and userData.
   *
   * @param personId
   *          - Specifying the target person.
   * @return A successful call returns the person's information.
   */
  public static JSONObject getPerson(String personId) {

    JSONObject jsonObject = null;

    HttpClient httpclient = HttpClients.createDefault();

    try {
      URIBuilder builder = new URIBuilder(API_REGION + "/persongroups/"
          + personGroupId + "/persons/" + personId);

      URI uri = builder.build();
      HttpGet request = new HttpGet(uri);
      request.setHeader("Ocp-Apim-Subscription-Key", API_KEY1);

      HttpResponse response = httpclient.execute(request);
      HttpEntity entity = response.getEntity();

      // If not null, format and display the JSON response.
      if (entity != null)
        jsonObject = new JSONObject(EntityUtils.toString(entity).trim());
    } catch (Exception e) {
      // Display error message.
      System.out.println(e.getMessage());
    }

    return jsonObject;
  }

}
