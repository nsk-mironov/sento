package io.sento.sample;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.sento.Bind;
import io.sento.Sento;

public class SampleFragment extends Fragment {
  public static SampleFragment newInstance() {
    return new SampleFragment();
  }

  @Bind(R.id.container) LinearLayout container;
  @Bind(R.id.firstname) TextView firstname;
  @Bind(R.id.lastname) TextView lastname;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_sample, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Sento.bind(this, view);

    firstname.setText("Hello");
    firstname.setTextColor(Color.BLACK);

    lastname.setText("Sento");
    lastname.setTextColor(Color.BLACK);

    container.setBackgroundColor(Color.WHITE);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    Sento.unbind(this);
  }
}
