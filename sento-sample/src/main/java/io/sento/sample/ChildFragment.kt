package io.sento.sample

import android.view.View
import io.sento.Bind

public class ChildFragment : ParentFragment() {
  private @Bind(R.id.container) val container: View = notNull()
}
