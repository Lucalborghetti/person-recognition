package main.java.constants;

/**
 * This class save the constant fields used by the program.
 */
public class Constants {

  // --------------------------------------------------------------
  // Common constants
  // --------------------------------------------------------------

  /** The current user working directory **/
  public static final String USER_WORKING_DIRECTORY    = System
      .getProperty("user.dir") + "/working";

  /** The current user temporary frames directory **/
  public static final String USER_FRAMESTEMP_DIRECTORY = System
      .getProperty("user.dir") + "/temp/";

  /** The format of the date we use to display **/
  public static final String DISPLAY_DATE_FORMAT       = "yyyy/MM/dd HH:mm:ss";

  /** The format of the date we use for the filename **/
  public static final String FILENAME_DATE_FORMAT      = "yyyyMMdd_HHmmss";

  // --------------------------------------------------------------
  // Amazon constants
  // --------------------------------------------------------------

  /** The collection id **/
  public static final String COLLECTION_ID             = "myAmazonCollection";

  /** The bucket name **/
  public static final String BUCKET_NAME               = "rekognition.bucket.oregon";

  /** The access key of AWS */

  public static final String ACCESS_KEY = "AKIAISUSPPJTGIJA2ACQ";

  /** The secret key of AWS */
  public static final String SECRET_KEY = "NAMqDLuf6Mm26g9mApLM7/tmo1QYZL5SL7B3XR5a";

  // --------------------------------------------------------------
  // Microsoft constants
  // --------------------------------------------------------------

  /** The Face API Name */
  public static final String API_NAME                  = "API_Cognitive_Services_Face_API";

  /** The first Face API Key */
  public static final String API_KEY1                  = "d961ef59ea004000b43a8ef9b608434c";

  /** The second Face API Key */
  public static final String API_KEY2                  = "c57950c4e2474c7e921dd92047d9fd0f";

  /** The Face API Region */
  public static final String API_REGION                = "https://westeurope.api.cognitive.microsoft.com/face/v1.0";

  /** The first Face API Key */
  public static final String STORAGE_ACCOUNT_NAME      = "storageheia";

  /** The second Face API Key */
  public static final String STORAGE_ACCOUNT_KEY       = "fEkoqNCoCV8IMTTHLuhmn2+1BVJmGBTzQdZ6qHFQnjtWxvpLqBF5yfVeANSuaRgTBS/Cr4xeyH6qu8ZpGOkBhw==";

  /** The storage connection string */
  public static final String STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=http;"
      + "AccountName=" + STORAGE_ACCOUNT_NAME + ";" + "AccountKey="
      + STORAGE_ACCOUNT_KEY;

  /** The path leading to the OpenCV classifier **/
  public static final String CLASSIFIER_PATH           = "src/main/resources/lbpcascades/lbpcascade_frontalface.xml";

}
