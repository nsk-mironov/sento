package com.github.vmironov.sento.sample;

import com.github.vmironov.sento.Binding;
import com.github.vmironov.sento.Finder;

public class SampleActivity$$SentoBinding implements Binding<SampleActivity> {
  @Override
  public <S> void bind(SampleActivity target, S source, Finder<? super S> finder) {
    target.firstname = finder.find(R.id.firstname, source);
    target.lastname = finder.find(R.id.lastname, source);
    target.avatar = finder.find(R.id.avatar, source);
  }

  @Override
  public void unbind(SampleActivity target) {
    target.firstname = null;
    target.lastname = null;
    target.avatar = null;
  }
}
