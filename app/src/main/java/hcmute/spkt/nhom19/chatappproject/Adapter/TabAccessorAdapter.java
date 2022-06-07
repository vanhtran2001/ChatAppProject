package hcmute.spkt.nhom19.chatappproject.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import hcmute.spkt.nhom19.chatappproject.Fragment.GroupFragment;
import hcmute.spkt.nhom19.chatappproject.Fragment.ChatFragment;
import hcmute.spkt.nhom19.chatappproject.Fragment.ContactFragment;
import hcmute.spkt.nhom19.chatappproject.Fragment.RequestFragment;

public class TabAccessorAdapter extends FragmentPagerAdapter {
    public TabAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1:
                GroupFragment groupFragment = new GroupFragment();
                return groupFragment;
            case 2:
                ContactFragment contactFragment = new ContactFragment();
                return contactFragment;
            case 3:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chat";
            case 1:
                return "Nhóm";
            case 2:
                return "Bạn bè";
            case 3:
                return "Yêu cầu";
            default:
                return null;
        }
    }
}
