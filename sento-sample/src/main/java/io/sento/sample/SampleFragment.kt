package io.sento.sample

import android.app.Fragment
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.sento.Bind
import io.sento.BindColor
import io.sento.BindDimen
import io.sento.BindString
import io.sento.OnClick
import io.sento.OnFocusChange
import io.sento.OnHover
import io.sento.OnKey
import io.sento.OnLongClick
import io.sento.OnTouch
import io.sento.Sento

public fun <T> notNull(): T = null as T

public class SampleFragment : Fragment() {
  private @Bind(R.id.first_name_input) val first_name_input: EditText = notNull()
  private @Bind(R.id.first_name_label) val first_name_label: TextView = notNull()

  private @Bind(R.id.last_name_input) val last_name_input: EditText = notNull()
  private @Bind(R.id.last_name_label) val last_name_label: TextView = notNull()

  private @BindString(R.string.first_name_label) val firstNameLabel: String = notNull()
  private @BindString(R.string.first_name_hint) val firstNameHint: String = notNull()

  private @BindString(R.string.last_name_label) val lastNameLabel: String = notNull()
  private @BindString(R.string.last_name_hint) val lastNameHint: String = notNull()

  private @BindDimen(R.dimen.padding_tiny) val paddingTiny = 0
  private @BindDimen(R.dimen.padding_small) val paddingSmall = 0
  private @BindDimen(R.dimen.padding_large) val paddingLarge = 0

  private @BindColor(R.color.color_primary) val colorPrimary = 0
  private @BindColor(R.color.color_background) val colorBackground = 0
  private @BindColor(R.color.color_text) val colorText = 0
  private @BindColor(R.color.color_hint) val colorHint = 0

  private @BindDimen(R.dimen.font_small) val fontSmall = 0.0f
  private @BindDimen(R.dimen.font_normal) val fontNormal = 0.0f

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.fragment_sample, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Sento.bind(this, view)

    view.background = ColorDrawable(colorBackground)
    view.setPadding(paddingLarge, paddingLarge, paddingLarge, paddingLarge)

    first_name_label.text = firstNameLabel
    first_name_label.setPadding(paddingSmall, paddingTiny, paddingSmall, paddingTiny)
    first_name_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSmall)
    first_name_label.setTextColor(colorPrimary)

    last_name_label.text = lastNameLabel
    last_name_label.setPadding(paddingSmall, paddingTiny, paddingSmall, paddingTiny)
    last_name_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSmall)
    last_name_label.setTextColor(colorPrimary)

    first_name_input.hint = firstNameHint
    first_name_input.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontNormal)
    first_name_input.setHintTextColor(colorHint)
    first_name_input.setTextColor(colorText)

    last_name_input.hint = lastNameHint
    last_name_input.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontNormal)
    last_name_input.setTextColor(colorText)
    last_name_input.setHintTextColor(colorHint)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    Sento.unbind(this)
  }

  private @OnClick(R.id.button_done) fun onDoneClick(view: View) {
    Toast.makeText(activity, "onDoneClick", Toast.LENGTH_SHORT).show()
  }

  private @OnClick(R.id.button_cancel) fun onCancelClick(view: View) {
    Toast.makeText(activity, "onCancelClick", Toast.LENGTH_SHORT).show()
  }

/*  private @OnTouch(R.id.button_cancel) fun onCancelTouch(view: View) {
    Toast.makeText(activity, "onCancelTouch", Toast.LENGTH_SHORT).show()
  }

  private @OnKey(R.id.button_cancel) fun onCancelKey(view: View) {
    Toast.makeText(activity, "onCancelKey", Toast.LENGTH_SHORT).show()
  }

  private @OnLongClick(R.id.button_cancel) fun onCancelLongClick(view: View) {
    Toast.makeText(activity, "onCancelLongClick", Toast.LENGTH_SHORT).show()
  }

  private @OnFocusChange(R.id.button_cancel) fun onCancelFocusChange(view: View) {
    Toast.makeText(activity, "onCancelFocusChange", Toast.LENGTH_SHORT).show()
  }

  private @OnHover(R.id.button_cancel) fun onCancelHover(view: View) {
    Toast.makeText(activity, "onCancelFocusChange", Toast.LENGTH_SHORT).show()
  }*/
}
