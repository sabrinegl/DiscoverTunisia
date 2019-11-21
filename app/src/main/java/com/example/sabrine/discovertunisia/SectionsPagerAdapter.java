package com.example.sabrine.discovertunisia;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.sabrine.discovertunisia.ui.fragments.ChatsFragment;
import com.example.sabrine.discovertunisia.ui.fragments.FriendsFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return  chatsFragment;

            case 2:
                GalleryFragment galleryFragment = new GalleryFragment();
                return galleryFragment;
            default:
                return  null;
        }


    }

    @Override
    public int getCount() {
        return 3;
    }



    public CharSequence getPageTitle(int position){

        switch (position) {
            case 0:
                return "AMIS";

            case 1:
                return "CHATS";

            case 2:
                return "GALLERY";

            default:
                return null;
        }

    }

}
