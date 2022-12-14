package edu.cmu.finddefinition;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
/**
 * Author: Zhongyue Zhang(zhongyue)
 * Last Modified: Nov 12, 2022
 *
 * This class provides capabilities to search for an image
 * as well as the dictionary definition for a given search term
 * The method search is the entry to the class
 * Network operations cannot be done from the UI thread,
 * therefore this class makes use of inner class BackgroundTask that
 * will do the network operation in a separate worker thread.
 * However, any UI updates should be done in the UI thread to avoid any synchronization
 * problems
 * onPostExecution runs in the UI thread, and it calls the ImageView pictureReady method to do the update.
 *
 * Method BackgroundTask.doInBackground( ) does the background work
 * Method BackgroundTask.onPostExecute( ) is called when the background work is
 *    done; it calls *back* to ip to report the results

 */

public class GetDefinition{
    FindDefinition ip = null;   // for callback
    String searchTerm = null;   // search word
    Bitmap picture = null;      // return picture
    String definition = null;   // return definition

    // search( )
    // Parameters:
    // String searchTerm: the thing to search for on flickr
    // Activity activity: the UI thread activity
    // InterestingPicture ip: the callback method's class; here, it will be ip.pictureReady( )
    public void search(String searchTerm, Activity activity, FindDefinition ip) {
        this.ip = ip;
        this.searchTerm = searchTerm;
        new BackgroundTask(activity).execute();
    }

    private class BackgroundTask {

        private Activity activity; // The UI thread

        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        public JSONObject getResult(String searchTerm) throws JSONException {
            //get the jsonObject from the given search term
            String pictureURL = null;
            String model = Build.MANUFACTURER;
            String HTTP_LINK = String.format("https://tranquil-tundra-69502.herokuapp.com/dictionary?model=%s&input=%s", model, searchTerm);

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(HTTP_LINK);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }
                JSONObject jsonObject = new JSONObject(buffer.toString());

                return jsonObject;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {

                    try {
                        doInBackground();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    // This is magic: activity should be set to MainActivity.this
                    //    then this method uses the UI thread
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }

        private void execute() {
            // There could be more setup here, which is why
            //    startBackground is not called directly
            startBackground();
        }

        // doInBackground( ) implements whatever you need to do on
        //    the background thread.
        // Implement this method to suit your needs
        private void doInBackground() throws IOException, JSONException {
            JSONObject jsonObject = getResult(searchTerm);
            try {
                definition = (String) jsonObject.get("definition");
                String pictureURL = (String) jsonObject.get("image_url");
                picture = searchPicture(pictureURL);
            }catch (Exception e){
                System.out.println("Empty Json");
            }

        }

        // onPostExecute( ) will run on the UI thread after the background
        //    thread completes.
        // Implement this method to suit your needs
        public void onPostExecute() {
            ip.pictureReady(picture, definition);
        }

        /*
         * Search Flickr.com for the searchTerm argument, and return a Bitmap that can be put in an ImageView
         */
        private Bitmap searchPicture(String pictureURL) throws IOException, JSONException {
            try {
                URL u = new URL(pictureURL);
                // Debugging:
                //System.out.println(pictureURL);
                return getRemoteImage(u);
            } catch (Exception e) {
                e.printStackTrace();
                return null; // so compiler does not complain
            }

        }


        /*
         * Given a URL referring to an image, return a bitmap of that image
         */
        @RequiresApi(api = Build.VERSION_CODES.P)
        private Bitmap getRemoteImage(final URL url) {
            try {
                final URLConnection conn = url.openConnection();
                conn.connect();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                Bitmap bm = BitmapFactory.decodeStream(bis);
                return bm;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

