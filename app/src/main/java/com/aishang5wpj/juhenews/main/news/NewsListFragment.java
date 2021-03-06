package com.aishang5wpj.juhenews.main.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aishang5wpj.juhenews.R;
import com.aishang5wpj.juhenews.app.BaseFragment;
import com.aishang5wpj.juhenews.bean.NewsBean;
import com.aishang5wpj.juhenews.bean.NewsChannelBean;
import com.aishang5wpj.juhenews.main.news.newsdetail.NewsDetailActivity;

import java.util.List;

/**
 * Created by wpj on 16/5/16上午10:41.
 */
public class NewsListFragment extends BaseFragment implements INewsView {

    private static final String CHANNEL = "channel";
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private INewsPresenter mNewsPresenter;
    private NewsChannelBean mChannel;
    private NewsListAdapter mNewsListAdapter;
    private LinearLayoutManager mLayoutManager;
    private int mPageIndex = 0;

    public static NewsListFragment newInstance(NewsChannelBean channel) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle data = new Bundle();
        data.putSerializable(CHANNEL, channel);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news_list;
    }

    @Override
    public void onInitViews() {

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.news_swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.news_recycler_view);
    }

    @Override
    public void onInitListeners() {
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mPageIndex = 0;
                mNewsPresenter.loadNews(mChannel, mPageIndex);
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == mNewsListAdapter.getItemCount()) {

                    //加载更多
                    mRefreshLayout.setRefreshing(true);
                    mNewsPresenter.loadNews(mChannel, mPageIndex);
                }
            }
        });
    }

    @Override
    public void onInitData() {

        mChannel = (NewsChannelBean) getArguments().getSerializable(CHANNEL);

        mNewsListAdapter = new NewsListAdapter();
        mNewsListAdapter.setOnItemClickListener(new NewsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, NewsBean newsBean) {

                Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
                intent.putExtra(NewsDetailActivity.NEWS_BEAN, newsBean);
                startActivity(intent);

                //使用下面的图片，SimpleDraweeView加载网络图片显示不出来：
                //1、使用fresco，不使用ActivityCompat.startActivity
                //2、使用ImageView，不使用fresco，则可以使用ActivityCompat.startActivity

                //http://blog.csdn.net/ljx19900116/article/details/41806889
//                View transitionView = view.findViewById(R.id.news_item_icon_iv);
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(), transitionView, getString(R.string.transition_news_img));
//
//                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });
        //设置布局管理器
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        //设置adapter
        mRecyclerView.setAdapter(mNewsListAdapter);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mNewsPresenter = new NewsPresenterImpl(this);
        mNewsPresenter.loadNews(mChannel, mPageIndex);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void showProgress() {
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onNewsLoad(List<NewsBean> newsBean) {

        List<NewsBean> newsList = newsBean;
        if (mPageIndex == 0) {

            mNewsListAdapter.setData(newsList);
        } else {

            mNewsListAdapter.addData(newsList);
        }
        //加载成功才使page加1
        mPageIndex++;
    }

    @Override
    public void runOnUiThread(Runnable runnable) {
        if (null == getActivity()) {
            return;
        }
        getActivity().runOnUiThread(runnable);
    }
}
