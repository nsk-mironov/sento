package io.sento.sample

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.sento.Sento
import io.sento.annotations.Bind
import io.sento.annotations.OnClick
import io.sento.annotations.OnLongClick
import io.sento.annotations.OnTextChanged
import io.sento.sample.annotations.OnFocusReceived
import io.sento.sample.extensions.asTrue
import io.sento.sample.extensions.notNull

class SampleFragment : Fragment() {
  private @Bind(R.id.first_name_input) val first_name_input: EditText? = notNull()
  private @Bind(R.id.first_name_label) val first_name_label: TextView? = notNull()

  private @Bind(R.id.last_name_input) val last_name_input: EditText = notNull()
  private @Bind(R.id.last_name_label) val last_name_label: TextView = notNull()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_sample, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Sento.bind(this, view)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    Sento.unbind(this)
  }

  private @OnClick(R.id.button_done) fun onDoneClick() {
    Toast.makeText(activity, "onDoneClick", Toast.LENGTH_SHORT).show()
  }

  private @OnClick(R.id.button_cancel) fun onCancelClick() {
    Toast.makeText(activity, "onCancelClick", Toast.LENGTH_SHORT).show()
  }

  private @OnFocusReceived(R.id.first_name_input) fun onFirstNameReceivedFocus() {
    Toast.makeText(activity, "focus changed to first name", Toast.LENGTH_SHORT).show()
  }

  private @OnFocusReceived(R.id.last_name_input) fun onLastNameReceivedFocus() {
    Toast.makeText(activity, "focus changed to last name", Toast.LENGTH_SHORT).show()
  }

  private @OnTextChanged(R.id.first_name_input) fun onFirstNameChanged(text: CharSequence) {
    Toast.makeText(activity, "first name changed: $text", Toast.LENGTH_SHORT).show()
  }

  private @OnTextChanged(R.id.last_name_input) fun onLastNameChanged(text: CharSequence) {
    Toast.makeText(activity, "last name changed: $text", Toast.LENGTH_SHORT).show()
  }

  private @OnLongClick(R.id.button_done, R.id.button_cancel) fun onLongClick(): Boolean {
    return Toast.makeText(activity, "onLongClick", Toast.LENGTH_SHORT).show().asTrue()
  }
}
