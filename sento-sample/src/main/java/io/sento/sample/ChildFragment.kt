package io.sento.sample

import android.view.View
import io.sento.Bind
import io.sento.Optional

public class ChildFragment : ParentFragment() {
  private @Bind(R.id.container) @Optional val container: View = notNull()
}
