package com.github.vmironov.sento.sample;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.vmironov.sento.Bind;
import com.github.vmironov.sento.Sento;

public class SampleActivity extends Activity {
  @Bind(R.id.fragment_container) FrameLayout container;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample);
    Sento.bind(this, this);

    if (savedInstanceState == null) {
      getFragmentManager()
          .beginTransaction()
          .add(container.getId(), SampleFragment.newInstance(), "SampleFragment")
          .commit();
    }
  }
}
