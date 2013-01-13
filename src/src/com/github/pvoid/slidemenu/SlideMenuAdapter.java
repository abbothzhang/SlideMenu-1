package com.github.pvoid.slidemenu;

import android.support.v4.app.Fragment;
import android.widget.BaseAdapter;
/**
 * Adapter for slide menu and content frame
 */
public abstract class SlideMenuAdapter extends BaseAdapter
{
  /**
   * Should return fragment for menu item
   * @param position position of menu item
   * @param id       identification of menu item
   * @return Fragment or null. If null will be returned content won't be changed
   */
  public abstract Fragment getFragment(int position, long id);
}
