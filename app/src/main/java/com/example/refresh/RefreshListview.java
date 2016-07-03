package com.example.refresh;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by wangzhenkai on 2016/6/27.
 */
public class RefreshListview extends ListView implements AbsListView.OnScrollListener {

    private float downY;
    private float moveY;
    private int measureheight;
    private View mHeadview;
    public static final int  PULL_REFRESH=0;
    public static final  int RELEASE_REFRESH=1;
    public static final  int refreshing=2;

    private int currentState;//当前状态
    private View headArrow;
    private TextView title;
    private TextView desc;
    private ProgressBar pb;
    private RotateAnimation rotateup;
    private RotateAnimation rotatedown;
    private int paddingtop;

    private OnRefreshListener refreshListenner;
    private View mfoot;
    private int footheight;
    private boolean isloadmore;//是否加载更多

    public RefreshListview(Context context) {
        super(context);

        initview();
    }

    public RefreshListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initview();
    }

    public RefreshListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initview();
    }

    /*
    * 初始化头布局 脚布局 滚动 监听
    * */
    private void initview() {
        initHeadview();

        initFootview();

        initAnimation();

        setOnScrollListener(this);
    }



    //初始化头部布局的动画
    private void initAnimation() {
        //向上转180 逆时针
        //围绕于自己中心
        rotateup = new RotateAnimation(0f,-180f,
                 Animation.RELATIVE_TO_SELF,0.5f,
                  Animation.RELATIVE_TO_SELF,0.5f
         );

        rotateup.setDuration(300);
        rotateup.setFillAfter(true);//动画停留在结束位置

        //向下转180 逆时针
        //围绕于自己中心
        rotatedown = new RotateAnimation(-180f,-360f,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f
        );

        rotatedown.setDuration(300);//时长
        rotatedown.setFillAfter(true);//动画停留在结束位置

    }

    //脚布局
    private void initFootview() {

        mfoot = View.inflate(getContext(), R.layout.foot,null);

        mfoot.measure(0,0);
        footheight = mfoot.getMeasuredHeight();

        //隐藏
        mfoot.setPadding(0,-footheight,0,0);
        addFooterView(mfoot);

    }
    //头布局
    private void initHeadview() {


        mHeadview = View.inflate(getContext(), R.layout.head,null);

        headArrow = mHeadview.findViewById(R.id.iv_arrow);
        title = (TextView) mHeadview.findViewById(R.id.tv_title);
        desc = (TextView) mHeadview.findViewById(R.id.yv_desc);
        pb = (ProgressBar) mHeadview.findViewById(R.id.idpb);


        //提前手动测量高度
        mHeadview.measure(0,0);//按照设置规则测量

      // int height= mHeadview.getHeight();//真实显示的高度
        //原始真实高度
        measureheight = mHeadview.getMeasuredHeight();

        //设置内边距可以隐藏当前控件
        mHeadview.setPadding(0,-measureheight,0,0);//隐藏自己的高度

        addHeaderView(mHeadview);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        //判断滑动距离 给Header 设置PaddingTop
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN://按下的位置

                downY = ev.getY();

                break;
            case MotionEvent.ACTION_MOVE://移动的位置

                //如果正在刷新中 就执行父类的处理
                if(currentState==refreshing){
                    return super.onTouchEvent(ev);
                }
                moveY = ev.getY();

                float offset= moveY - downY;//偏移量
              //只有啊偏移量>0 并且当前第一个可见条目索引是0 才放大头部

                if(offset>0&&getFirstVisiblePosition()==0){

                    paddingtop = (int) (-measureheight+offset);
                    mHeadview.setPadding(0, paddingtop,0,0);

                    if(paddingtop >=0&&currentState!=RELEASE_REFRESH){//完全显示

                        //变成释放刷新模式
                        currentState = RELEASE_REFRESH;

                        updataHeader();//根据最新状态更换头布局内容

                    }else if(paddingtop <0&& currentState!=PULL_REFRESH){
                        //切换成下拉刷新模式
                        currentState=PULL_REFRESH;
                        updataHeader();
                    }

                    return true;//表示当前事件被我们处理并销毁

                }

                break;
            case MotionEvent.ACTION_UP:

              //  mHeadview.getPaddingTop();
                if(paddingtop<0){
                    mHeadview.setPadding(0,-measureheight,0,0);
                }else {
                    mHeadview.setPadding(0,0,0,0);
                    currentState=refreshing;
                    updataHeader();
                }

                //或者根据刚刚设置的状态判断
                break;
        }
        return super.onTouchEvent(ev);
    }

    //根据状态更新布局内容
    private void updataHeader() {

        switch (currentState){
            case PULL_REFRESH://切换回下拉刷新

                headArrow.startAnimation(rotatedown);
                title.setText("下拉刷新");
                break;
            case RELEASE_REFRESH://切换成释放刷新
                //做动画 改标题
                headArrow.startAnimation(rotateup);
                title.setText("释放刷新");

                break;
            case refreshing://刷新中……

                headArrow.clearAnimation();
                headArrow.setVisibility(View.GONE);
                pb.setVisibility(View.VISIBLE);
                title.setText("zhengzaishauxinzhong ");

                if(refreshListenner!=null){
                    refreshListenner.onRefresh();//通知调用者
                }

                break;
            default:
                break;

        }
    }

    //刷新完毕回复界面
    public void hide() {

        if(isloadmore){

            //加载更多
            mfoot.setPadding(0,-footheight,0,0);
            isloadmore=false;
        }else {
            //上拉刷新
            currentState = PULL_REFRESH;
            title.setText("下拉刷新");//切换文本
            mHeadview.setPadding(0, -measureheight, 0, 0);//左上右下 距离
            pb.setVisibility(GONE);
            headArrow.setVisibility(VISIBLE);

            String time = getTime();
            desc.setText("shijian " + time);
        }
    }

    private String getTime() {
        long currentTime= System.currentTimeMillis();
       SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.format(currentTime);
    }



    public interface OnRefreshListener{
        void onRefresh();

        void loadMore();
    }

    public void setRefreshListenner(OnRefreshListener refreshListenner) {

        this.refreshListenner=refreshListenner;
    }





    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        //状态改变
//        public static  int  IDLE=0;//空闲
//        public static int touch=1;//触摸
//        public static int fling=2;//滑翔

        if(isloadmore){
            return;//已经是加载跟多 返回
        }
        //最新状态空闲 并且当前界面 显示所有数据的最后一条
      if(scrollState==SCROLL_STATE_IDLE&&getLastVisiblePosition()>=getCount()-1){

          isloadmore = true;

          mfoot.setPadding(0,0,0,0);
          setSelection(getCount());//转到最后一条

          if(refreshListenner!=null){
              refreshListenner.loadMore();
          }
      }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //滑动过程中
    }


}
