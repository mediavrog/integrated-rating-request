package net.mediavrog.samples.irr;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.mediavrog.irr.DefaultRuleEngine;
import net.mediavrog.irr.IrrAdapterDecorator;

import java.util.ArrayList;
import java.util.List;

public class DefaultEngineAsyncListActivity extends DefaultEngineActivity implements
        LoaderManager.LoaderCallbacks<List<String>> {
    public static final String TAG = DefaultEngineAsyncListActivity.class.getSimpleName();

    public interface OnPositionNearEndListener {
        void onNearEnd();
    }

    private static final int LOADER = 1;

    private IrrAdapterDecorator irrDecoAdapter;
    private List<String> mEntries;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_default_engine_list;
    }

    @Override
    protected void initialize() {
        ListView lv = (ListView) findViewById(android.R.id.list);

        mEntries = new ArrayList<>();

        getSupportLoaderManager().initLoader(LOADER, new Bundle(), DefaultEngineAsyncListActivity.this);
        ArrayAdapter myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mEntries) {
            private static final int TYPE_EVEN = 0;
            private static final int TYPE_ODD = 1;

            protected static final int NEAR_END_THRESHOLD = 3;
            private OnPositionNearEndListener mNearEndListener = new OnPositionNearEndListener() {
                @Override
                public void onNearEnd() {
                    final Loader l = getSupportLoaderManager().getLoader(LOADER);
                    if (l != null && ((ListDataLoader) l).hasMoreResults() && !((ListDataLoader) l).isLoading()) {
                        l.forceLoad();
                    }
                }
            };

            public boolean isNearEndOfList(int position) {
                return getCount() - position <= NEAR_END_THRESHOLD;
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position) {
                return position % 2 == 0 ? TYPE_EVEN : TYPE_ODD;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (mNearEndListener != null && isNearEndOfList(position))
                    mNearEndListener.onNearEnd();
                View v = super.getView(position, convertView, parent);
                if(getItemViewType(position) == TYPE_EVEN) v.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                return v;
            }
        };

        // show the irr view at the 40th position (index starts at 0)
        // don't pass a rule engine here but use the default implementation provided by the irr itself
        irrDecoAdapter = new IrrAdapterDecorator(this, myAdapter, 39, R.layout.standard_irr_layout);
        lv.setAdapter(irrDecoAdapter);
        engine = (DefaultRuleEngine) irrDecoAdapter.getUnderlyingRuleEngine();

        // change demo description
        ((TextView) findViewById(R.id.intro)).setText(R.string.default_engine_async_list_intro);
    }

    @Override
    void evaluateRules(boolean onlyDump) {
        if (!onlyDump) irrDecoAdapter.notifyRuleEngineStateChanged();
        super.evaluateRules(onlyDump);
    }


    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle args) {
        return new ListDataLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
        Toast.makeText(this, (data.isEmpty() ? "Reached end of list" : "More elements loaded"), Toast.LENGTH_SHORT).show();
        mEntries.addAll(data);
        irrDecoAdapter.notifyDataSetChanged(true);
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {

    }

    public static class ListDataLoader extends AsyncTaskLoader<List<String>> {
        private int offset = 0;
        private boolean mHasNext = true;
        private boolean mIsLoading;
        private static final int perPage = 10;

        public ListDataLoader(Context context) {
            super(context);
            mHasNext = true;
        }

        /**
         * This is where the bulk of our work is done.  This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override
        public List<String> loadInBackground() {
            ArrayList<String> entries = new ArrayList<>();

            if (mHasNext && !mIsLoading) {
                mIsLoading = true;

                // simulate long running operation
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // no more results after page was loaded 10 times
                if (offset < perPage * 10) {
                    for (int i = offset; i < offset + perPage; i++)
                        entries.add("A fancy list item #" + (i + 1));
                }

                if (entries.isEmpty()) {
                    mHasNext = false;
                } else {
                    mHasNext = entries.size() >= perPage;
                    // prepare for next load by incrementing the offset
                    offset += perPage;
                }
            }
            return entries;
        }

        /**
         * Called when there is new data to deliver to the client.  The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
        @Override
        public void deliverResult(List<String> entries) {
            mIsLoading = false;

            if (isReset()) return;

            if (isStarted()) {
                // Need to return new ArrayList for some reason or onLoadFinished() is not called
                super.deliverResult(new ArrayList<>(entries));
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override
        protected void onStopLoading() {
            mIsLoading = false;
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();
        }

        private boolean hasMoreResults() {
            return mHasNext;
        }

        private boolean isLoading() {
            return mIsLoading;
        }
    }
}
