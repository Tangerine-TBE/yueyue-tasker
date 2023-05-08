package cn.com.auto.thkl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.compose.ui.unit.TextUnitType.Companion.Sp
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.multidex.MultiDexApplication
import cn.com.auto.thkl.autojs.AutoJs
import cn.com.auto.thkl.autojs.key.GlobalKeyObserver
import cn.com.auto.thkl.receiver.DynamicBroadcastReceivers
import cn.com.auto.thkl.service.AccessibilityService
import cn.com.auto.thkl.utils.SP
import cn.com.auto.thkl.weight.ThemeColorManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.stardust.app.GlobalAppContext
import com.stardust.autojs.core.ui.inflater.ImageLoader
import com.stardust.autojs.core.ui.inflater.util.Drawables
import com.stardust.theme.ThemeColor
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import org.autojs.autojs.timing.TimedTaskManager
import org.autojs.autojs.timing.TimedTaskScheduler
import java.lang.ref.WeakReference

/**
 * Created by Stardust on 2017/1/27.
 */

class App : MultiDexApplication() {
    lateinit var dynamicBroadcastReceivers: DynamicBroadcastReceivers
        private set
    override fun onCreate() {
        super.onCreate()
        GlobalAppContext.set(
            this, com.stardust.app.BuildConfig.generate(BuildConfig::class.java)
        )
        instance = WeakReference(this)
        handler = Handler(Looper.getMainLooper())
        init()
        /*ui 375 667 */
        AutoSize.initCompatMultiProcess(this)
        AutoSizeConfig.getInstance().setDesignHeightInDp(667)
            .setDesignWidthInDp(375).onAdaptListener = object : onAdaptListener {
            override fun onAdaptBefore(target: Any?, activity: Activity?) {
            }

            override fun onAdaptAfter(target: Any?, activity: Activity?) {
            }
        }
    }


    private fun init() {
        SP.init(this)
        ThemeColorManagerCompat.init(
            this,
            ThemeColor(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark),
                ContextCompat.getColor(this, R.color.colorAccent)
            )
        )
        AutoJs.initInstance(this)
        /*音量键控制*/
        GlobalKeyObserver.init()
        setupDrawableImageLoader()
        TimedTaskScheduler.init(this)
        initDynamicBroadcastReceivers()
    }


    @SuppressLint("CheckResult")
    private fun initDynamicBroadcastReceivers() {
        dynamicBroadcastReceivers = DynamicBroadcastReceivers(this)
        val localActions = ArrayList<String>()
        val actions = ArrayList<String>()
        TimedTaskManager.allIntentTasks
            .filter { task -> task.action != null }
            .doOnComplete {
                if (localActions.isNotEmpty()) {
                    dynamicBroadcastReceivers.register(localActions, true)
                }
                if (actions.isNotEmpty()) {
                    dynamicBroadcastReceivers.register(actions, false)
                }
                @Suppress("DEPRECATION")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                    Intent(
                        DynamicBroadcastReceivers.ACTION_STARTUP
                    )
                )
            }
            .subscribe({
                if (it.isLocal) {
                    it.action?.let { it1 -> localActions.add(it1) }
                } else {
                    it.action?.let { it1 -> actions.add(it1) }
                }
            }, { it.printStackTrace() })


    }

    private fun setupDrawableImageLoader() {
        Drawables.setDefaultImageLoader(object : ImageLoader {
            override fun loadInto(imageView: ImageView, uri: Uri) {
                Glide.with(imageView)
                    .load(uri)
                    .into(imageView)
            }

            override fun loadIntoBackground(view: View, uri: Uri) {
                Glide.with(view)
                    .load(uri)
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            view.background = resource
                        }
                    })
            }

            override fun load(view: View, uri: Uri): Drawable {
                throw UnsupportedOperationException()
            }

            override fun load(
                view: View,
                uri: Uri,
                drawableCallback: ImageLoader.DrawableCallback
            ) {
                Glide.with(view)
                    .load(uri)
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            drawableCallback.onLoaded(resource)
                        }
                    })
            }

            override fun load(view: View, uri: Uri, bitmapCallback: ImageLoader.BitmapCallback) {
                Glide.with(view)
                    .asBitmap()
                    .load(uri)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bitmapCallback.onLoaded(resource)
                        }
                    })
            }
        })
    }

    companion object {
        private lateinit var instance: WeakReference<App>
        lateinit var service:AccessibilityService
        public lateinit var handler:Handler
        val app: App
            get() = instance.get()!!
    }


}
