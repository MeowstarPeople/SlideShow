package com.miaoxingren.slideshow.application;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import android.app.Application;
import android.content.Context;


public class SlideApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		initImageLoader(getContext());
	}
	/** 
	 * ImageLoader 图片组件初始化 
	 * @param context 
	 */  
	private void initImageLoader(Context context) {  
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.denyCacheImageMultipleSizesInMemory()
		.diskCacheFileNameGenerator(new Md5FileNameGenerator())
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.writeDebugLogs()   
		.build();  
		ImageLoader.getInstance().init(config);  
	}
	private Context getContext(){
		return getApplicationContext();
	}
}
