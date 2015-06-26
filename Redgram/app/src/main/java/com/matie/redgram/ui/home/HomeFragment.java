package com.matie.redgram.ui.home;


import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.matie.redgram.R;
import com.matie.redgram.data.managers.presenters.HomePresenterImpl;
import com.matie.redgram.ui.common.base.BaseComponent;
import com.matie.redgram.ui.common.base.BaseFragment;
import com.matie.redgram.ui.common.main.MainComponent;
import com.matie.redgram.ui.home.views.HomeView;
import com.matie.redgram.ui.home.views.widgets.postlist.PostRecyclerView;
import com.nineoldandroids.view.ViewHelper;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by matie on 17/01/15.
 */
public class HomeFragment extends BaseFragment implements HomeView, ObservableScrollViewCallbacks{
    @InjectView(R.id.home_recycler_view)
    PostRecyclerView homeRecyclerView;

    Toolbar mToolbar;
    View mContentView;
    LinearLayoutManager mLayoutManager;

    HomeComponent component;

    @Inject
    HomePresenterImpl homePresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);

        homeRecyclerView.setScrollViewCallbacks(this);

        this.mLayoutManager = (LinearLayoutManager)homeRecyclerView.getLayoutManager();
        this.mToolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
        this.mContentView = getActivity().findViewById(R.id.container);

        return view;
    }


    @Override
    protected void setupComponent(MainComponent mainComponent) {
        component = DaggerHomeComponent.builder()
                    .mainComponent(mainComponent)
                    .homeModule(new HomeModule(this))
                    .build();
        //component.inject(this);

        //todo: find another way to use injected instances
        homePresenter = (HomePresenterImpl)component.getHomePresenter();
        //todo: call in a separate method
        homePresenter.populateView();
    }

    @Override
    public HomeComponent component() {
        return component;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        homePresenter.registerForEvents();
    }

    @Override
    public void onStop() {
        homePresenter.unregisterForEvents();
        super.onStop();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (toolbarIsShown()) {
               hideToolbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
            }
        }
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showInfoMessage() {

    }

    @Override
    public void showErrorMessage() {

    }


    @Override
    public void showToolbar() {
        moveToolbar(0);
    }

    @Override
    public void hideToolbar() {
        moveToolbar(-mToolbar.getHeight());
    }

    @Override
    public PostRecyclerView getRecyclerView() {
        return homeRecyclerView;
    }

    @Override
    public Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    public boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mToolbar) == 0;
    }

    public boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(mToolbar) == -mToolbar.getHeight();
    }

    private void moveToolbar(float toTranslationY) {
        if (ViewHelper.getTranslationY(mToolbar) == toTranslationY) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(mToolbar), toTranslationY).setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                ViewHelper.setTranslationY(mToolbar, translationY);
                ViewHelper.setTranslationY((View) homeRecyclerView, translationY);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ((View) homeRecyclerView).getLayoutParams();
                lp.height = (int) -translationY + mContentView.getHeight() - lp.topMargin;
                ((View) homeRecyclerView).requestLayout();
            }
        });
        animator.start();
    }

}