package bachelor.claudiu.interactiveinformationshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class InteractiveInformationShareActivity extends AppCompatActivity {

    private TextView intentReceivedTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_information_share);

        intentReceivedTextView = (TextView)findViewById(R.id.intent_received_text_view);
        Intent intent = getIntent();
        if (intent != null) {
            String textShared = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (textShared != null)
            {
                intentReceivedTextView.setText(textShared);
            }
            else
            {
                intentReceivedTextView.setText("ERROR! :(");
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
