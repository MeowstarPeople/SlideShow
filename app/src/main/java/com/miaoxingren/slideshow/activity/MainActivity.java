package com.miaoxingren.slideshow.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.miaoxingren.slideshow.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;

public class MainActivity extends Activity {

    private final static boolean isAutoPlay = true;  //自动轮播启用开关
    private String[] imageUrls;  //自定义轮播图的资源
    private List<ImageView> imageViewsList;  //放轮播图片的ImageView 的list
    private List<View> dotViewsList;  //放圆点的View的list
    private ViewPager viewPager;
    private int currentItem = 0;  //当前轮播页
    private ScheduledExecutorService scheduledExecutorService; //定时任务
    private Context context;


    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .resetViewBeforeLoading(false)  //  设置图片在加载前是否重置、复位
            .cacheInMemory(true) //   设置下载的图片是否缓存在内存中
            .cacheOnDisk(true) //   设置下载的图片是否缓存在SD卡中
            .displayer(new FadeInBitmapDisplayer(100))// 图片加载好后渐入的动画时间
            .displayer(new SimpleBitmapDisplayer()) //  还可以设置圆角图片new RoundedBitmapDisplayer(20)
            .handler(new Handler())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_slideshow);
        initData();
        if (isAutoPlay) {
            startPlay();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentItem);
        }
    };

    /**
     * 开始轮播图切换
     */
    private void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 1, 4, TimeUnit.SECONDS);
    }

    /**
     * 初始化相关Data
     */
    private void initData() {
        context = getApplicationContext();
        imageViewsList = new ArrayList<>();
        dotViewsList = new ArrayList<>();
        new GetListTask().execute("");
    }

    /**
     * 初始化Views等UI
     */
    private void initUI(Context context) {
        if (imageUrls == null || imageUrls.length == 0)
            return;
        LinearLayout dotLayout = (LinearLayout) findViewById(R.id.dotLayout);
        dotLayout.removeAllViews();
        // 热点个数与图片特殊相等
        for (int i = 0; i < imageUrls.length; i++) {
            ImageView view = new ImageView(context);
            view.setTag(imageUrls[i]);
            if (i == 0)//给一个默认图
                view.setBackgroundResource(R.drawable.appmain_subject_1);
            view.setScaleType(ScaleType.FIT_XY);
            imageViewsList.add(view);
            ImageView dotView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = 5;
            params.rightMargin = 5;
            dotLayout.addView(dotView, params);
            dotViewsList.add(dotView);
        }
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setFocusable(true);
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.addOnPageChangeListener(new MyPageChangeListener());
    }

    /**
     * 填充ViewPager的页面适配器
     */
    private class MyPagerAdapter extends PagerAdapter {
        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(imageViewsList.get(position));
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ImageView imageView = imageViewsList.get(position);
            ImageLoader.getInstance().displayImage(imageView.getTag() + "", imageView, options);//显示图片
            ((ViewPager) container).addView(imageViewsList.get(position));
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("com.miaoxingren.slideshow.information");
                    startActivity(intent);
                }
            });
            return imageViewsList.get(position);
        }

        @Override
        public int getCount() {
            return imageViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    /**
     * ViewPager的监听器
     * 当ViewPager中页面的状态发生改变时调用
     */
    private class MyPageChangeListener implements OnPageChangeListener {

        boolean isAutoPlay = false;

        @Override
        public void onPageScrollStateChanged(int postion) {
            switch (postion) {
                case 1:// 手势正在滑动
                    isAutoPlay = false;
                    break;
                case 2:// 滑动完毕
                    isAutoPlay = true;
                    break;
                case 0: //什么都没做
                    // 当前为最后一张，此时从右向左滑，则切换到第一张
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        viewPager.setCurrentItem(0);
                    }
                    // 当前为第一张，此时从左向右滑，则切换到最后一张
                    else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                    }
                    break;
            }
        }

        @Override
        public void onPageSelected(int pos) {
            currentItem = pos;
            for (int i = 0; i < dotViewsList.size(); i++) {
                if (i == pos) {
                    ((View) dotViewsList.get(pos)).setBackgroundResource(R.drawable.dot_focus);
                } else {
                    ((View) dotViewsList.get(i)).setBackgroundResource(R.drawable.dot_blur);
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }

    /**
     * 执行轮播图切换任务
     */
    private class SlideShowTask implements Runnable {
        @Override
        public void run() {
            synchronized (viewPager) {
                currentItem = (currentItem + 1) % imageViewsList.size();
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    /**
     * 异步任务,获取数据
     */
    class GetListTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                imageUrls = new String[]{
                        "http://banbao.chazidian.com/uploadfile/2016-01-25/s145368924044608.jpg",
                        "http://pic32.nipic.com/20130829/12906030_124355855000_2.png",
                        "http://pic36.nipic.com/20131217/6704106_233034463381_2.jpg",
                        "http://pic41.nipic.com/20140509/4746986_145156378323_2.jpg"
                };
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                initUI(context);
            }
        }
    }


}
