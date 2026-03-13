package com.team.eleven.fragment;

import android.content.Context;
import android.os.Bundle;

import com.team.eleven.databinding.FragmentHomeBinding;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.team.eleven.APICallingPackage.Class.APIRequestManager;
import com.team.eleven.APICallingPackage.Interface.ResponseManager;
import com.team.eleven.R;
import com.team.eleven.utils.SessionManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements ResponseManager {
    ResponseManager responseManager;
    APIRequestManager apiRequestManager;
    SessionManager sessionManager;
    FragmentHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        responseManager = this;
        apiRequestManager = new APIRequestManager(getActivity());
        sessionManager = new SessionManager();

        setupViewPager(binding.viewpager);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.viewpager, new FragmentFixtures());
        transaction.commit();

        return binding.getRoot();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new FragmentFixtures(), getResources().getString(R.string.cricket));

        binding.viewpager.setAdapter(adapter);
        binding.FragmentTab.setupWithViewPager(viewPager);

        for (int i = 0; i < binding.FragmentTab.getTabCount(); i++) {
            TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            binding.FragmentTab.getTabAt(i).setCustomView(tv);
        }

        viewPager.setOffscreenPageLimit(1);

    }


    @Override
    public void getResult(Context mContext, String type, String message, JSONObject result) {

    }


    @Override
    public void onError(Context mContext, String type, String message) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}