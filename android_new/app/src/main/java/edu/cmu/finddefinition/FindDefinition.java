package edu.cmu.finddefinition;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
/**
 * Author: Zhongyue Zhang(zhongyue)
 * Last Modified: Nov 12, 2022
 * It will conduct the interaction with the UI thread

 */
public class FindDefinition extends AppCompatActivity{

    FindDefinition me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * The click listener will need a reference to this object, so that upon successfully finding a picture, it
         * can callback to this object with the resulting picture Bitmap.
         * The "this" of the OnClick will be the OnClickListener, not
         * this InterestingPicture.
         */
        final FindDefinition ma = this;

        /*
         * Find the "submit" button, and add a listener to it
         */
        Button submitButton = (Button)findViewById(R.id.submit);


        // Add a listener to the send button
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                String searchTerm = ((EditText)findViewById(R.id.searchTerm)).getText().toString();
                System.out.println("searchTerm = " + searchTerm);
                GetDefinition gp = new GetDefinition();
                gp.search(searchTerm, me, ma); // Done asynchronously in another thread.  It calls ip.pictureReady() in this thread when complete.
            }
        });
    }

    /*
     * This is called by the GetDefinition object when the picture is ready.
     * This allows for passing back the Bitmap picture for updating the ImageView
     * as well as updating the definition TextView
     */
    public void pictureReady(Bitmap picture, String definition) {
        ImageView pictureView = (ImageView)findViewById(R.id.FindDefinition);
        TextView searchView = (EditText)findViewById(R.id.searchTerm);
        TextView notifyView = (TextView) findViewById(R.id.textView2);
        TextView definitionView = (TextView) findViewById(R.id.textView);
        TextView labelView = (TextView) findViewById(R.id.textView3);
        String searchTerm = ((EditText)findViewById(R.id.searchTerm)).getText().toString();

        notifyView.setText("");
        definitionView.setText("");
        labelView.setText("");

        if(picture==null && definition==null){
            pictureView.setImageResource(R.mipmap.ic_launcher);
            System.out.println("No picture");
            pictureView.setVisibility(View.INVISIBLE);
            notifyView.setText("Sorry, could not find a picture and definition of the "+searchTerm);
        }else if(picture == null){
            pictureView.setImageResource(R.mipmap.ic_launcher);
            System.out.println("No picture");
            pictureView.setVisibility(View.INVISIBLE);
            notifyView.setText("Sorry, could not find a picture of the "+searchTerm);
            labelView.setText("Definition:");
            definitionView.setText(definition);
        }else if(definition == null){
            pictureView.setImageBitmap(picture);
            System.out.println("picture");
            pictureView.setVisibility(View.VISIBLE);
            notifyView.setText("Sorry, could find the definition of the "+searchTerm);
        }else{
            pictureView.setImageBitmap(picture);
            System.out.println("picture");
            pictureView.setVisibility(View.VISIBLE);
            notifyView.setText("Successfully find the definition and picture of the "+searchTerm);
            labelView.setText("Definition:");
            definitionView.setText(definition);

        }
        searchView.setText("");
        pictureView.invalidate();
    }

}
