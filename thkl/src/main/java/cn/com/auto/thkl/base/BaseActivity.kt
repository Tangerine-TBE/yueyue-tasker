package cn.com.auto.thkl.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.com.auto.thkl.dialog.ProcessDialog
import io.reactivex.disposables.Disposable


abstract class BaseActivity : AppCompatActivity() {
    private var TAG: String = javaClass.simpleName

    private val mDisposable: ArrayList<Disposable> = ArrayList()
    private var mProcessDialog: ProcessDialog ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mProcessDialog = ProcessDialog(this)
        when (val contentView = initialize()) {
            is Int -> {
                setContentView(contentView)
            }
            is View -> {
                setContentView(contentView)
            }
            else -> {
                throw RuntimeException("$TAG:--the Activity Xml must be Int or View!")
            }
        }
        setStatusBar()
        initUI()
        initListener()
    }

    public fun addDisposable(it: Disposable) {
        mDisposable.add(it)
    }

    abstract fun setStatusBar()
    abstract fun initialize(): Any
    abstract fun initUI()
    abstract fun initListener()
    fun showProcessDialog() {
        mProcessDialog?.show()
    }

    fun hideProcessDialog() {
        if (mProcessDialog != null){
            if (mProcessDialog?.isShowing!!) {
                mProcessDialog?.dismiss()
                mProcessDialog?.cancel()
            }
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mProcessDialog != null){
            if (mProcessDialog!!.isShowing){
                mProcessDialog!!.dismiss()
            }
        }
        for (it in mDisposable) {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }


}