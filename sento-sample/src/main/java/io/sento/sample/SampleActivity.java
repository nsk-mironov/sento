package io.sento.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;
import io.sento.Bind;
import io.sento.Sento;

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
