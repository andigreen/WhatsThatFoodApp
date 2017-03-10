package com.wtf.whatsthatfoodapp.memory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wtf.whatsthatfoodapp.R;

public class ShareTextActivity extends AppCompatActivity {

    private EditText textEntry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_text);

        Button shareButton = (Button)findViewById(R.id.share_text_button);
        textEntry = (EditText)findViewById(R.id.share_text_entry);

        shareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String userEntry = textEntry.getText().toString();

                Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                textShareIntent.putExtra(Intent.EXTRA_TEXT, userEntry);
                textShareIntent.setType("text/plain");
                startActivity(Intent.createChooser(textShareIntent, "Share text with..."));
            }
        });
    }
}
