package cn.com.auto.thkl.script

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.Nullable
import cn.com.auto.thkl.Pref
import cn.com.auto.thkl.R
import cn.com.auto.thkl.autojs.AutoJs
import cn.com.auto.thkl.fileprovider.AppFileProvider
import com.stardust.app.GlobalAppContext
import com.stardust.autojs.execution.ExecutionConfig
import com.stardust.autojs.execution.ScriptExecution
import com.stardust.autojs.execution.ScriptExecutionListener
import com.stardust.autojs.execution.SimpleScriptExecutionListener
import com.stardust.autojs.runtime.exception.ScriptInterruptedException
import com.stardust.autojs.script.ScriptSource
import com.stardust.autojs.script.StringScriptSource
import com.stardust.util.IntentUtil
import org.autojs.autojs.model.script.ScriptFile
import org.mozilla.javascript.RhinoException
import java.io.File
import java.io.FileFilter

/**
 * Created by Stardust on 2017/5/3.
 */

object Scripts {

    const val ACTION_ON_EXECUTION_FINISHED = "ACTION_ON_EXECUTION_FINISHED"
    const val EXTRA_EXCEPTION_MESSAGE = "message"
    const val EXTRA_EXCEPTION_LINE_NUMBER = "lineNumber"
    const val EXTRA_EXCEPTION_COLUMN_NUMBER = "columnNumber"

    val FILE_FILTER = FileFilter { file ->
        file.isDirectory || file.name.endsWith(".js")
                || file.name.endsWith(".auto")
    }

    private val BROADCAST_SENDER_SCRIPT_EXECUTION_LISTENER = object : SimpleScriptExecutionListener() {

        override fun onSuccess(execution: ScriptExecution, result: Any?) {
            GlobalAppContext.get().sendBroadcast(Intent(ACTION_ON_EXECUTION_FINISHED))
        }

        override fun onException(execution: ScriptExecution, e: Throwable) {
            val rhinoException = getRhinoException(e)
            var line = -1
            var col = 0
            if (rhinoException != null) {
                line = rhinoException.lineNumber()
                col = rhinoException.columnNumber()
            }
            if (ScriptInterruptedException.causedByInterrupted(e)) {
                GlobalAppContext.get().sendBroadcast(Intent(ACTION_ON_EXECUTION_FINISHED)
                        .putExtra(EXTRA_EXCEPTION_LINE_NUMBER, line)
                        .putExtra(EXTRA_EXCEPTION_COLUMN_NUMBER, col))
            } else {
                GlobalAppContext.get().sendBroadcast(Intent(ACTION_ON_EXECUTION_FINISHED)
                        .putExtra(EXTRA_EXCEPTION_MESSAGE, e.message)
                        .putExtra(EXTRA_EXCEPTION_LINE_NUMBER, line)
                        .putExtra(EXTRA_EXCEPTION_COLUMN_NUMBER, col))
            }
        }

    }


    fun openByOtherApps(uri: Uri) {
        IntentUtil.viewFile(GlobalAppContext.get(), uri, "text/plain", AppFileProvider.AUTHORITY)
    }

    fun openByOtherApps(file: File) {
        openByOtherApps(Uri.fromFile(file))
    }







    fun run(file: ScriptFile): ScriptExecution? {
        return try {
            AutoJs.getInstance().scriptEngineService.execute(file.toSource(),
                    ExecutionConfig(workingDirectory = file.parent))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(GlobalAppContext.get(), e.message, Toast.LENGTH_LONG).show()
            null
        }

    }


    fun run(source: ScriptSource): ScriptExecution? {
        return try {
            AutoJs.getInstance().scriptEngineService.execute(source, ExecutionConfig(workingDirectory = Pref.getScriptDirPath()))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(GlobalAppContext.get(), e.message, Toast.LENGTH_LONG).show()
            null
        }

    }
    fun run(stringSource:String,listener:ScriptExecutionListener):ScriptExecution?{
        val source = StringScriptSource(stringSource)
        return try {
            AutoJs.getInstance().scriptEngineService.execute(source,listener,ExecutionConfig(workingDirectory = Pref.getScriptDirPath()))
        }catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(GlobalAppContext.get(),e.message,Toast.LENGTH_LONG).show()
            null
        }
    }
    fun run (stringSource:String) :ScriptExecution?{
        val source = StringScriptSource(stringSource)
        return try {
            AutoJs.getInstance().scriptEngineService.execute(source,ExecutionConfig(workingDirectory = Pref.getScriptDirPath()))
        }catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(GlobalAppContext.get(),e.message,Toast.LENGTH_LONG).show()
            null
        }
    }

    fun runWithBroadcastSender(file: File): ScriptExecution {
        return AutoJs.getInstance().scriptEngineService.execute(
            ScriptFile(file).toSource(), BROADCAST_SENDER_SCRIPT_EXECUTION_LISTENER,
                ExecutionConfig(workingDirectory = file.parent))
    }


    fun runRepeatedly(scriptFile: ScriptFile, loopTimes: Int, delay: Long, interval: Long): ScriptExecution {
        val source = scriptFile.toSource()
        val directoryPath = scriptFile.parent
        return AutoJs.getInstance().scriptEngineService.execute(source, ExecutionConfig(workingDirectory = directoryPath,
                delay = delay, loopTimes = loopTimes, interval = interval))
    }

    @Nullable
    fun getRhinoException(throwable: Throwable?): RhinoException? {
        var e = throwable
        while (e != null) {
            if (e is RhinoException) {
                return e
            }
            e = e.cause
        }
        return null
    }

    fun send(file: ScriptFile) {
        val context = GlobalAppContext.get()
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_STREAM, IntentUtil.getUriOfFile(context, file.path, AppFileProvider.AUTHORITY)),
                GlobalAppContext.getString(R.string.text_send)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    }
}