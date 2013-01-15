package com.github.pvoid.slidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.Interpolator;
import android.widget.*;
/**
 * View with slide to right content and menu panel behind it
 */
public class SlideMenuPanel extends RelativeLayout implements AdapterView.OnItemClickListener, View.OnTouchListener
{
  private static final int MAX_SETTLE_DURATION = 600; // ms
  private static final int INVALID_POINTER = -1;
  private static final int TAIL_PART = 4;

  private float _mInitialX;
  private float _mLastX;
  private float _mLastY;
  private int _mPointerId = INVALID_POINTER;
  private int _mTouchSlop;
  private boolean _mProcessMove = false;
  private boolean _mIgnoreMove = false;
  private VelocityTracker _mVelocityTracker;
  private Scroller _mScroller;
  private ViewGroup _mScrollPanel = null;
  private float _mMinimumVelocity;
  private float _mMaximumVelocity;
  private int _mPanelWidth;
  private FragmentManager _mManager;
  private SlideMenuAdapter _mAdapter;

  private static final Interpolator _sInterpolator = new Interpolator()
  {
    public float getInterpolation(float t)
    {
      t -= 1.0f;
      return t * t * t * t * t + 1.0f;
    }
  };
  /**
   * Constructor
   * @param context context for vew
   */
  @SuppressWarnings("unused")
  public SlideMenuPanel(Context context)
  {
    super(context);
///// Setting up main UI
    setupUI(context);
  }
  /**
   * Constructor
   * @param context context for vew
   * @param attrs   attributes for view from xml file
   */
  @SuppressWarnings("unused")
  public SlideMenuPanel(Context context, AttributeSet attrs)
  {
    super(context, attrs);
///// Setting up main UI
    setupUI(context);
  }
  /**
   * Constructor
   * @param context  context for vew
   * @param attrs    attributes for view from xml file
   * @param defStyle theme for view
   */
  @SuppressWarnings("unused")
  public SlideMenuPanel(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
///// Setting up main UI
    setupUI(context);
  }
  /**
   * Define adapter for menu and content
   * @param adapter adapter for menu and content
   * @param manager fragment manager. Will be used for attaching fragment
   */
  public void setAdapter(SlideMenuAdapter adapter, FragmentManager manager)
  {
    _mAdapter = adapter;
    _mManager = manager;
    ListView list = (ListView)findViewById(R.id.navigation_list);
    if(list!=null)
      list.setAdapter(adapter);
  }
  /**
   * Create view appearance
   * @param context context for view
   */
  protected void setupUI(Context context)
  {
    final ViewConfiguration configuration = ViewConfiguration.get(context);
    _mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
////// Setting up navigation list
    ListView list = new ListView(context);
    list.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    list.setId(R.id.navigation_list);
    list.setOnItemClickListener(this);
    addView(list);
////// Setting up scroll panel
    _mScrollPanel = new FrameLayout(context);
    _mScrollPanel.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    _mScrollPanel.setId(R.id.scroll_panel);
    addView(_mScrollPanel);
////// Setting up content panel
    FrameLayout panel = new FrameLayout(context);
    panel.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    panel.setId(R.id.content_panel);
////// Setting up background color
    TypedArray attrs = context.getTheme().obtainStyledAttributes(new int[] {android.R.attr.colorBackground});
    panel.setBackgroundColor(attrs.getColor(0, Color.RED));
    attrs.recycle();
    panel.setOnTouchListener(this);
    _mScrollPanel.addView(panel);
//////
    _mScroller = new Scroller(context, _sInterpolator);
    _mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    _mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
  }
  /**
   * Measure view size
   * @param widthMeasureSpec specs for width
   * @param heightMeasureSpec specs for height
   */
  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
  {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
////// Calculate panel width
    _mPanelWidth = getMeasuredWidth()/TAIL_PART;
    View list = findViewById(R.id.navigation_list);
    LayoutParams params = (LayoutParams)list.getLayoutParams();
    params.setMargins(0,0,_mPanelWidth,0);
    list.setLayoutParams(params);
  }
  /**
   * Capture all childs touch events
   * @param ev touch event
   * @return true to dismiss all subsequent touch events
   */
  @Override
  public boolean onInterceptTouchEvent(final MotionEvent ev)
  {
    int action = ev.getAction() & MotionEvent.ACTION_MASK;
////// Check action
    if(action==MotionEvent.ACTION_DOWN)
    {
      _mPointerId = MotionEventCompat.getPointerId(ev, 0);
      _mInitialX = _mScrollPanel.getScrollX();
      _mLastX = ev.getX();
      _mLastY = ev.getY();
      _mProcessMove = false;
      _mIgnoreMove = false;
      _mScroller.abortAnimation();
    }
    else if(action==MotionEvent.ACTION_MOVE && _mPointerId!=INVALID_POINTER && !_mIgnoreMove)
    {
      final int pointerIndex = MotionEventCompat.findPointerIndex(ev,_mPointerId);
      final float dx = Math.abs(MotionEventCompat.getX(ev,pointerIndex) - _mLastX);
      final float dy = Math.abs(MotionEventCompat.getY(ev,pointerIndex) - _mLastY);

      if(dx>_mTouchSlop && dx>dy)
      {
        _mProcessMove = true;
      }
      else if(dy>_mTouchSlop)
      {
        _mIgnoreMove = true;
      }
    }
    else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL)
    {
      endDrag();
    }
////// Obtain velocity tracker
    if(_mVelocityTracker==null)
      _mVelocityTracker = VelocityTracker.obtain();
////// Adding event
    _mVelocityTracker.addMovement(ev);
//////
    return _mProcessMove;
  }
  /**
   * Touch event occurred
   * @param event touch event
   * @return true if we process event
   */
  @Override
  public boolean onTouchEvent(final MotionEvent event)
  {
    int action = event.getAction() & MotionEvent.ACTION_MASK;
    if(!_mProcessMove)
      return false;
//////
    if(_mVelocityTracker==null)
      _mVelocityTracker = VelocityTracker.obtain();
    _mVelocityTracker.addMovement(event);
////// Check action
    if(action==MotionEvent.ACTION_DOWN)
      _mScroller.abortAnimation();
    else if(action==MotionEvent.ACTION_MOVE)
    {
      final int pointerIndex = MotionEventCompat.findPointerIndex(event,_mPointerId);
      final float x = MotionEventCompat.getX(event,pointerIndex);
      float dx = _mLastX - x;
      _mLastX = x;
      if(_mScrollPanel!=null)
      {
        int scrollX = _mScrollPanel.getScrollX();
        if(dx>0)
        {
          if(scrollX==0)
            return true;
          if(scrollX+dx>0)
            dx = -scrollX;
        }
        else
        {
          if(-scrollX==getMeasuredWidth()-_mPanelWidth)
            return true;
          if(scrollX+dx-_mPanelWidth<-getMeasuredWidth())
          {
            _mScrollPanel.scrollTo(-getMeasuredWidth() + _mPanelWidth, 0);
            return true;
          }
        }
        _mScrollPanel.scrollBy((int)dx,0);
      }
    }
    else if(action==MotionEvent.ACTION_UP)
    {
      _mVelocityTracker.computeCurrentVelocity(1000, _mMaximumVelocity);
//////// Let's find direction of scroll
      int offsetWidth = getMeasuredWidth();
      int targetX;
      int velocity = (int)VelocityTrackerCompat.getXVelocity(_mVelocityTracker, _mPointerId);
////////// Switch according velocity
      if(Math.abs(velocity)>_mMinimumVelocity)
        targetX = velocity<0 ? 0 : (-offsetWidth+_mPanelWidth);
      else
      {
        int center = offsetWidth/2;
        int scroll = _mScrollPanel.getScrollX();
        if(Math.abs(_mInitialX)<center)
        {
          if(Math.abs(scroll)>_mPanelWidth)
            targetX = -offsetWidth+_mPanelWidth;
          else
            targetX = 0;
        }
        else
        {
          if(Math.abs(scroll)<center)
            targetX = 0;
          else
            targetX = -offsetWidth+_mPanelWidth;
        }
      }
////////
      startSmoothScroll(targetX,velocity);
      endDrag();
    }
    else if(action==MotionEvent.ACTION_CANCEL)
      endDrag();
////// We will process event
    return true;
  }
  /**
   * Stops slide panel drag
   */
  private void endDrag()
  {
    _mProcessMove = false;
    _mIgnoreMove  = false;
    _mPointerId   = INVALID_POINTER;
    if(_mVelocityTracker!=null)
    {
      _mVelocityTracker.recycle();
      _mVelocityTracker = null;
    }
  }
  /**
   * Starts smooth scroll to target x
   * @param targetX  scroll target
   * @param velocity scroll velocity
   */
  private void startSmoothScroll(int targetX, int velocity)
  {
    if(_mScrollPanel!=null)
    {
      int x = _mScrollPanel.getScrollX();
      int duration;
      int dx = targetX - x;
/////////
      velocity = Math.abs(velocity);
      if(velocity>0)
      {
        duration = 4 * Math.round(1000 * Math.abs((float)dx / velocity));
      }
      else
      {
        final float pageDelta = (float) Math.abs(dx) / getMeasuredWidth();
        duration = (int) ((pageDelta + 1) * 100);
      }
      duration = Math.min(duration, MAX_SETTLE_DURATION);
      _mScroller.startScroll(x,0,dx,0,duration);
      _mScrollPanel.scrollTo(_mScroller.getCurrX()-1,_mScroller.getCurrY());
      ViewCompat.postInvalidateOnAnimation(_mScrollPanel);
    }
  }
  /**
   * Compute scroll position
   */
  @Override
  public void computeScroll()
  {
    if(_mScrollPanel!=null && !_mScroller.isFinished() && _mScroller.computeScrollOffset())
    {
      int oldX = _mScrollPanel.getScrollX();
      int oldY = _mScrollPanel.getScrollY();
      int x = _mScroller.getCurrX();
      int y = _mScroller.getCurrY();
      if(oldX != x || oldY != y)
        _mScrollPanel.scrollTo(x,y);
      else
        _mScroller.forceFinished(true);
    }
  }
  /**
   * Click on menu item
   * @param adapterView list with menu items
   * @param view tapped view
   * @param index menu item index
   * @param id menu item id
   */
  public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long id)
  {
    if(_mAdapter!=null && _mManager!=null)
    {
      Fragment fragment = _mAdapter.getFragment(index, id);
      if(fragment!=null)
        _mManager.beginTransaction().replace(R.id.content_panel,fragment).commit();
    }
    startSmoothScroll(0,0);
  }
  /**
   * Touch event occurred
   * @param view touched view
   * @param motionEvent touch event
   * @return always true
   */
  public boolean onTouch(final View view, final MotionEvent motionEvent)
  {
    return true;
  }
}
