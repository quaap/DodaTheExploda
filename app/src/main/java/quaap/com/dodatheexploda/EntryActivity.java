package quaap.com.dodatheexploda;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class EntryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        LinearLayout mv = (LinearLayout)findViewById(R.id.main_view);

        for (final Mode m: Mode.values()) {
            Button b = new Button(this);
            b.setText(m.name());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(EntryActivity.this, MainActivity.class);
                    i.putExtra("mode", m.name());
                    startActivity(i);
                }
            });
            mv.addView(b);
        }

    }
}
