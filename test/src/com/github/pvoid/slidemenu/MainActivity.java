package com.github.pvoid.slidemenu;

import android.R;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends FragmentActivity
{
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    SlideMenuPanel panel = new SlideMenuPanel(this);
    panel.setAdapter(new SlideAdapter(this),getSupportFragmentManager());
    setContentView(panel);
  }

  private static class SlideAdapter extends SlideMenuAdapter
  {
    private final LayoutInflater _mInflater;

    SlideAdapter(Context context)
    {
      _mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Fragment getFragment(final int position, final long id)
    {
      if(position==4)
        return new TestFragment2();

      Bundle arguments = new Bundle();
      arguments.putInt("index",position+1);
      Fragment fragment = new TestFragment();
      fragment.setArguments(arguments);
      return fragment;
    }

    public int getCount()
    {
      return 15;
    }

    public Object getItem(final int index)
    {
      return "Line number "+(index+1);
    }

    public long getItemId(final int index)
    {
      return 0;
    }

    public View getView(final int index, View view, final ViewGroup viewGroup)
    {
      if(view==null)
        view = _mInflater.inflate(R.layout.simple_list_item_1,viewGroup,false);

      TextView text = (TextView)view.findViewById(R.id.text1);
      if(text!=null)
        text.setText("Line number "+(index+1));
      return view;
    }
  }

  private static class TestFragment extends Fragment
  {
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
      TextView view = new TextView(inflater.getContext());
      view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
      view.setBackgroundColor(Color.WHITE);
      view.setTextColor(Color.BLACK);
      view.setGravity(Gravity.CENTER);
      view.setText("Fragment "+getArguments().getInt("index"));
      view.setScrollContainer(true);
      return view;
    }
  }

  private static class TestFragment2 extends ListFragment
  {
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState)
    {
      super.onViewCreated(view, savedInstanceState);
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.simple_list_item_1);
      for(int index=0;index<20;++index)
        adapter.add("Content line "+(index+1));
      setListAdapter(adapter);
    }
  }

}
