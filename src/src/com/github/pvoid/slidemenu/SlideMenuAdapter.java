package com.github.pvoid.slidemenu;

import android.support.v4.app.Fragment;
import android.widget.BaseAdapter;

public abstract class SlideMenuAdapter extends BaseAdapter
{
  public abstract Fragment getFragment(int position, long id);
}
