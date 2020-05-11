package com.ajou.capstone_design_freitag.ui.mypage;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.ajou.capstone_design_freitag.API.RESTAPI;
import com.ajou.capstone_design_freitag.LoginActivity;
import com.ajou.capstone_design_freitag.R;
import com.google.android.material.tabs.TabLayout;

public class MyPageFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RESTAPI instance = RESTAPI.getInstance();
        //토큰 받아오는데 null이면 로그인
        if(instance.getToken()==null){
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            //화면전환했을때 뒤로가기하면 프로젝트 생성화면이 보이는 문제생김 방지
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        TabLayout tabs = (TabLayout) view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("의뢰한 프로젝트 목록"));
        tabs.addTab(tabs.newTab().setText("완료한 작업 목록"));
        tabs.setTabGravity(tabs.GRAVITY_FILL);

        //어뎁터 부분
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        final PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(),2);
        viewPager.setAdapter(pagerAdapter);

        //탭 선택 이벤트처리
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        return view;
    }



}
