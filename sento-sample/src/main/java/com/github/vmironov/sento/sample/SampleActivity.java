package com.github.vmironov.sento.sample;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.vmironov.sento.Bind;
import com.github.vmironov.sento.Sento;

public class SampleActivity extends Activity {
  @Bind(R.id.firstname) TextView firstname;
  @Bind(R.id.lastname) TextView lastname;
  @Bind(R.id.avatar) ImageView avatar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample);
    Sento.bind(this, this);

    firstname.setText("Hello");
    lastname.setText("Sento");

    avatar.setImageDrawable(new ColorDrawable(Color.RED));
  }
}
