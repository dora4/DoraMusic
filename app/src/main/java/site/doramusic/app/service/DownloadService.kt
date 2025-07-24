package site.doramusic.app.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.text.TextUtils
import dora.db.dao.DaoFactory
import dora.util.RxBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import site.doramusic.app.event.DeleteResponseEvent
import site.doramusic.app.event.DeleteTaskEvent
import site.doramusic.app.event.DownloadMessage
import site.doramusic.app.model.DownloadTask
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DownloadService : Service() {

    private var disposable: CompositeDisposable? = null

    // 线程池
    private val singleExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // 加载队列
    private val downloadTaskQueue: MutableList<DownloadTask> = Collections.synchronizedList(
        ArrayList<DownloadTask>()
    )

    // Handler
    private lateinit var handler: Handler

    // 包含所有的DownloadTask
    private var downloadTaskList: MutableList<DownloadTask>? = null

    private var downloadListener: OnDownloadListener? = null
    private var isBusy = false
    private var isCancel = false

    override fun onCreate() {
        super.onCreate()
        handler = Handler(mainLooper)
        // 从数据库中获取所有的任务
        downloadTaskList = DaoFactory.getDao(DownloadTask::class.java).selectAll().toMutableList()
    }

    override fun onBind(intent: Intent?): IBinder {
        return TaskBuilder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 接受创建的DownloadTask
        val disposable: Disposable = RxBus.getInstance()
            .toObservable(DownloadTask::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { task ->
                // 判断任务是否为轮询标志
                // 判断任务是否存在，并修改任务
                if (TextUtils.isEmpty(task.getTaskName())
                    || !checkAndAlterDownloadTask(task)
                ) {
                    addToExecutor(task)
                }
            }
        addDisposable(disposable)

        // 是否删除数据的问题
        val deleteDisposable: Disposable = RxBus.getInstance()
            .toObservable(DeleteTaskEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { task ->
                // 判断是否该数据存在加载列表中
                var isDelete = true
                for (bean in downloadTaskQueue) {
                }
                // 如果不存在则删除List中的task
                if (isDelete) {
                    val taskIt: Iterator<DownloadTask> =
                        downloadTaskList!!.iterator()
                    while (taskIt.hasNext()) {
                        val task: DownloadTask = taskIt.next()
                    }
                }
                // 返回状态
                RxBus.getInstance().post(DeleteResponseEvent(isDelete, task.music))
            }
        addDisposable(deleteDisposable)
        return super.onStartCommand(intent, flags, startId)
    }


    /**
     * 1. 查看是否任务已存在
     * 2. 修改DownloadTask的 taskName 和 list
     *
     * @return
     */
    private fun checkAndAlterDownloadTask(newTask: DownloadTask): Boolean {
        var isExist = false
        for (downloadTask in downloadTaskList!!) {
            // 如果不相同则不往下执行，往下执行都是存在相同的情况
            if (!downloadTask.getTaskName().equals(newTask.getTaskName())) continue

            if (downloadTask.status == DownloadTask.STATUS_FINISH) {

            } else {
                isExist = true
                // 发送回去：已经在加载队列中
                postMessage("任务已存在")
            }
        }
        // 重置名字
        if (!isExist) {
            postMessage("成功添加到缓存队列")
        }
        return isExist
    }

    private fun addToExecutor(task: DownloadTask) {
        // 判断是否为轮询请求
        if (!TextUtils.isEmpty(task.getTaskName())) {
            if (!downloadTaskList!!.contains(task)) {
                // 加入总列表中，表示创建
                downloadTaskList!!.add(task)
            }
            // 添加到下载队列
            downloadTaskQueue.add(task)
        }
        // 从队列顺序取出第一条下载
        if (downloadTaskQueue.size > 0 && !isBusy) {
            isBusy = true
            executeTask(downloadTaskQueue[0])
        }
    }

    private fun executeTask(task: DownloadTask) {
        val runnable = Runnable {
            task.status = DownloadTask.STATUS_LOADING
            var result = LOAD_NORMAL
            if (result == LOAD_NORMAL) {
                // 存储DownloadTask的状态
                task.status = DownloadTask.STATUS_FINISH
                // 发送完成状态
                postDownloadChange(task, DownloadTask.STATUS_FINISH, "下载完成")
            } else if (result == LOAD_ERROR) {
                task.status = DownloadTask.STATUS_ERROR
                // 任务加载失败
                postDownloadChange(task, DownloadTask.STATUS_ERROR, "资源或网络错误")
            } else if (result == LOAD_PAUSE) {
                task.status = DownloadTask.STATUS_PAUSE
                postDownloadChange(task, DownloadTask.STATUS_PAUSE, "暂停加载")
            } else if (result == LOAD_DELETE) {
            }

            // 存储状态
            DaoFactory.getDao(DownloadTask::class.java).insertOrUpdate(task)

            // 轮询下一个事件，用RxBus用来保证事件是在主线程

            // 移除完成的任务
            downloadTaskQueue.remove(task)
            // 设置为空闲
            isBusy = false
            // 轮询
            post(DownloadTask())
        }
        singleExecutor.execute(runnable)
    }

    private fun postDownloadChange(task: DownloadTask, status: Int, msg: String) {
        if (downloadListener != null) {
            val position = downloadTaskList!!.indexOf(task)
            // 通过handler，切换回主线程
            handler.post {
                downloadListener!!.onDownloadChange(
                    position, status, msg
                )
            }
        }
    }

    private fun postMessage(msg: String) {
        RxBus.getInstance().post(DownloadMessage(msg))
    }

    private fun post(task: DownloadTask) {
        RxBus.getInstance().post(task)
    }


    override fun onUnbind(intent: Intent?): Boolean {
        downloadListener = null
        return super.onUnbind(intent)
    }

    internal inner class TaskBuilder : Binder(), IDownloadManager {
        override val downloadTaskList: List<Any>
            get() = Collections.unmodifiableList(this@DownloadService.downloadTaskList)

        override fun setOnDownloadListener(listener: OnDownloadListener) {
            downloadListener = listener
        }

        override fun setDownloadStatus(taskName: String, status: Int) {
            // 修改某个Task的状态
            when (status) {
                DownloadTask.STATUS_WAIT -> {
                    var i = 0
                    while (i < this@DownloadService.downloadTaskList!!.size) {
                        val bean: DownloadTask = this@DownloadService.downloadTaskList!![i]
                        if (taskName == bean.getTaskName()) {
                            bean.status = DownloadTask.STATUS_WAIT
                            downloadListener!!.onDownloadResponse(i, DownloadTask.STATUS_WAIT)
                            addToExecutor(bean)
                            break
                        }
                        ++i
                    }
                }

                DownloadTask.STATUS_PAUSE -> {
                    val it: Iterator<DownloadTask> = downloadTaskQueue.iterator()
                    while (it.hasNext()) {
                        val bean: DownloadTask = it.next()
                        if (bean.getTaskName().equals(taskName)) {
                            if (bean.status == DownloadTask.STATUS_LOADING
                                && bean.getTaskName().equals(taskName)
                            ) {
                                isCancel = true
                                break
                            } else {
                                bean.status = DownloadTask.STATUS_PAUSE
                                downloadTaskQueue.remove(bean)
                                val position = this@DownloadService.downloadTaskList!!.indexOf(bean)
                                downloadListener!!.onDownloadResponse(
                                    position,
                                    DownloadTask.STATUS_PAUSE
                                )
                                break
                            }
                        }
                    }
                }
            }
        }

        override fun setAllDownloadStatus(status: Int) {
            // 修改所有Task的状态
        }
        // 首先判断是否在加载队列中。
        // 如果在加载队列中首先判断是否正在下载，
        // 然后判断是否在完成队列中。
    }

    interface IDownloadManager {

        val downloadTaskList: List<Any>

        fun setOnDownloadListener(listener: OnDownloadListener)

        fun setDownloadStatus(taskName: String, status: Int)

        fun setAllDownloadStatus(status: Int)
    }

    interface OnDownloadListener {

        /**
         * @param pos    : Task在item中的位置
         * @param status : Task的状态
         * @param msg:   传送的Msg
         */
        fun onDownloadChange(pos: Int, status: Int, msg: String?)

        /**
         * 回复
         */
        fun onDownloadResponse(pos: Int, status: Int)
    }

    companion object {
        private const val TAG = "DownloadService"

        // 加载状态
        private const val LOAD_ERROR = -1
        private const val LOAD_NORMAL = 0
        private const val LOAD_PAUSE = 1
        private const val LOAD_DELETE = 2

        // 下载状态
        val STATUS_RESUME: Int = DownloadTask.STATUS_LOADING
        val STATUS_PAUSE: Int = DownloadTask.STATUS_PAUSE
    }

    private fun addDisposable(disposable: Disposable?) {
        if (this.disposable == null) {
            this.disposable = CompositeDisposable()
        }
        this.disposable!!.add(disposable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null) {
            disposable!!.dispose()
        }
    }
}
