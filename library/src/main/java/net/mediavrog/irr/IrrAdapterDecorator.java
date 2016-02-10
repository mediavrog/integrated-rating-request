package net.mediavrog.irr;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import net.mediavrog.ruli.RuleEngine;

/**
 * Adopted from https://raw.githubusercontent.com/nhaarman/ListViewAnimations/master/lib-core/src/main/java/com/nhaarman/listviewanimations/BaseAdapterDecorator.java
 */
/*
 * Copyright 2014 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A decorator class that decorates an instance of {@link BaseAdapter} with an Integrated Rating Request element at a given position.
 */
public class IrrAdapterDecorator extends BaseAdapter {
    public static final String TAG = IrrAdapterDecorator.class.getSimpleName();
    private static final int IRR_ITEM_TYPE = 0;

    /**
     * The {@link android.widget.BaseAdapter} this {@code BaseAdapterDecorator} decorates.
     */
    @NonNull
    private final BaseAdapter mDecoratedBaseAdapter;

    /**
     * The position where an IntegratedRatingRequestLayout should be injected.
     */
    private int mIrrElementPosition;

    /**
     * The layout used to inflate an irr element.
     */
    @NonNull
    private IrrLayout mIrrLayout;

    /**
     * Should the Irr layout be attached to the list.
     */
    private boolean mIsShown;

    /**
     * Create a new {@code BaseAdapterDecorator}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter the {@code} BaseAdapter to decorate.
     * @param irrPosition the position of the irr element.
     */
    public IrrAdapterDecorator(Context ctx, @NonNull final BaseAdapter baseAdapter, int irrPosition, int irrLayoutId) {
        this(ctx, baseAdapter, irrPosition, irrLayoutId, null);
    }

    public IrrAdapterDecorator(Context ctx, @NonNull final BaseAdapter baseAdapter, int irrPosition, int irrLayoutId, @Nullable RuleEngine engine) {
        this(baseAdapter, irrPosition, (IrrLayout) LayoutInflater.from(ctx).inflate(irrLayoutId, null, false), engine);
    }

    public IrrAdapterDecorator(@NonNull final BaseAdapter baseAdapter, int irrPosition, @NonNull IrrLayout irrLayout, @Nullable RuleEngine engine) {
        mDecoratedBaseAdapter = baseAdapter;
        mIrrElementPosition = irrPosition;
        mIrrLayout = irrLayout;
        mIrrLayout.setOnToggleVisibilityListener(new IrrLayout.OnToggleVisibilityListener() {

            @Override
            public void onShow(IrrLayout irr) {
                mIsShown = true;
                notifyDataSetChanged(true);
            }

            @Override
            public void onHide(IrrLayout irr) {
                mIsShown = false;
                notifyDataSetChanged(true);
            }
        });

        if (engine != null) mIrrLayout.setRuleEngine(engine);
        notifyRuleEngineStateChanged();
    }

    /**
     * Returns the {@link android.widget.BaseAdapter} that this {@code BaseAdapterDecorator} decorates.
     */
    @NonNull
    public BaseAdapter getDecoratedBaseAdapter() {
        return mDecoratedBaseAdapter;
    }

    /**
     * Returns the root {@link android.widget.BaseAdapter} this {@code BaseAdapterDecorator} decorates.
     */
    @NonNull
    protected BaseAdapter getRootAdapter() {
        BaseAdapter adapter = mDecoratedBaseAdapter;
        while (adapter instanceof IrrAdapterDecorator) {
            adapter = ((IrrAdapterDecorator) adapter).getDecoratedBaseAdapter();
        }
        return adapter;
    }

    /**
     * Increments the decorated item count by 1.
     */
    @Override
    public int getCount() {
        return mDecoratedBaseAdapter.getCount() + getOffset();
    }

    @Override
    public Object getItem(final int position) {
        return mDecoratedBaseAdapter.getItem(getRealPosition(position));
    }

    @Override
    public long getItemId(final int position) {
        return mDecoratedBaseAdapter.getItemId(getRealPosition(position));
    }

    @Override
    @NonNull
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        if (mIsShown && getItemViewType(position) == IRR_ITEM_TYPE) {
            if (convertView instanceof IrrLayout) {
                return convertView;
            } else {
                return mIrrLayout;
            }
        } else {
            return mDecoratedBaseAdapter.getView(getRealPosition(position), convertView, parent);
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mDecoratedBaseAdapter.areAllItemsEnabled();
    }

    @Override
    @NonNull
    public View getDropDownView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        return mDecoratedBaseAdapter.getDropDownView(getRealPosition(position), convertView, parent);
    }

    /**
     * Offsets the decorated item view types by 1.
     */
    @Override
    public int getItemViewType(final int position) {
        if (mIsShown && position == mIrrElementPosition) {
            return IRR_ITEM_TYPE;
        } else {
            return mDecoratedBaseAdapter.getItemViewType(getRealPosition(position)) + 1;
        }
    }

    /**
     * Increments the decorated item view type count by 1.
     */
    @Override
    public int getViewTypeCount() {
        return mDecoratedBaseAdapter.getViewTypeCount() + 1;
    }

    @Override
    public boolean hasStableIds() {
        return mDecoratedBaseAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mDecoratedBaseAdapter.isEmpty();
    }

    @Override
    public boolean isEnabled(final int position) {
        return mDecoratedBaseAdapter.isEnabled(getRealPosition(position));
    }

    @Override
    public void notifyDataSetChanged() {
        if (!(mDecoratedBaseAdapter instanceof ArrayAdapter<?>)) {
            // fix #35 dirty trick !
            // leads to an infinite loop when trying because ArrayAdapter triggers notifyDataSetChanged itself
            // TODO: investigate
            mDecoratedBaseAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Helper function if you want to force notifyDataSetChanged()
     */
    @SuppressWarnings("UnusedDeclaration")
    public void notifyDataSetChanged(final boolean force) {
        if (force || !(mDecoratedBaseAdapter instanceof ArrayAdapter<?>)) {
            // leads to an infinite loop when trying because ArrayAdapter triggers notifyDataSetChanged itself
            // TODO: investigate
            mDecoratedBaseAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetInvalidated() {
        mDecoratedBaseAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void registerDataSetObserver(@NonNull final DataSetObserver observer) {
        mDecoratedBaseAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(@NonNull final DataSetObserver observer) {
        mDecoratedBaseAdapter.unregisterDataSetObserver(observer);
    }

    public RuleEngine getUnderlyingRuleEngine() {
        return mIrrLayout.getRuleEngine();
    }

    public void notifyRuleEngineStateChanged() {
        mIsShown = mIrrLayout.getRuleEngine().evaluate();
        notifyDataSetChanged(true);
    }

    /**
     * Converts the given position into the decorated adapter's position, taking the added irr element into account.
     *
     * @param position
     * @return
     */
    private int getRealPosition(int position) {
        return position > mIrrElementPosition ? position - getOffset() : position;
    }

    private int getOffset() {
        return mIsShown ? 1 : 0;
    }
}
