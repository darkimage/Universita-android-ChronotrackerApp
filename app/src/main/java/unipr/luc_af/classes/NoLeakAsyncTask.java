package unipr.luc_af.classes;

import android.app.Activity;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class NoLeakAsyncTask<I, P, R> extends AsyncTask<I, P, R> {
    private WeakReference<Activity> mActivityWeakReference;
    private BackgroundTask<I, R> mTask;
    private PostTask<R> mPostTask;
    private ErrorTask mErrorTask;
    private ThreadErrorTask mThreadErrorTask;
    private Exception mTaskException;
    private ThreadPostTask mThreadPostTask;

    public NoLeakAsyncTask(Activity context, BackgroundTask<I, R> task) {
        mActivityWeakReference = new WeakReference<>(context);
        mTask = task;
    }

    public NoLeakAsyncTask(Activity context, BackgroundTask<I, R> task, PostTask<R> postTask) {
        mActivityWeakReference = new WeakReference<>(context);
        mTask = task;
        mPostTask = postTask;
    }

    public NoLeakAsyncTask(Activity context, BackgroundTask<I, R> task, PostTask<R> postTask, ErrorTask errorTask) {
        mActivityWeakReference = new WeakReference<>(context);
        mTask = task;
        mPostTask = postTask;
        mErrorTask = errorTask;
    }

    public NoLeakAsyncTask(Activity context, BackgroundTask<I, R> task, PostTask<R> postTask, ErrorTask errorTask, ThreadPostTask threadPostTask) {
        mActivityWeakReference = new WeakReference<>(context);
        mTask = task;
        mPostTask = postTask;
        mErrorTask = errorTask;
        mThreadPostTask = threadPostTask;
    }

    public NoLeakAsyncTask(Activity context, BackgroundTask<I, R> task, PostTask<R> postTask, ErrorTask errorTask, ThreadErrorTask threadErrorTask, ThreadPostTask threadPostTask) {
        mActivityWeakReference = new WeakReference<>(context);
        mTask = task;
        mPostTask = postTask;
        mErrorTask = errorTask;
        mThreadErrorTask = threadErrorTask;
        mThreadPostTask = threadPostTask;
    }

    @Override
    protected R doInBackground(I... is) {
        try {
            return mTask.executeTask(is);
        } catch (Exception error) {
            mTaskException = error;
            if (mThreadErrorTask != null) {
                mThreadErrorTask.executeTask(error);
            }
            return null;
        } finally {
            if (mThreadPostTask != null) {
                mThreadPostTask.executeTask();
            }
        }
    }

    @Override
    protected void onPostExecute(R r) {
        Activity activity = mActivityWeakReference.get();
        if (activity != null || !activity.isFinishing()) {
            if (mPostTask != null && mTaskException == null) {
                mPostTask.executeTask(r);
            } else if (mTaskException != null) {
                if (mErrorTask != null) {
                    mErrorTask.executeTask(mTaskException);
                }
            }
        }
    }

    public interface BackgroundTask<I, R> {
        R executeTask(I... is) throws Exception;
    }

    public interface PostTask<R> {
        void executeTask(R in);
    }

    public interface ErrorTask {
        void executeTask(Throwable error);
    }

    public interface ThreadErrorTask {
        void executeTask(Throwable error);
    }

    public interface ThreadPostTask {
        void executeTask();
    }
}
