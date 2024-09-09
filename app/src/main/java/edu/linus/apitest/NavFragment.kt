package edu.linus.apitest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class NavFragment extends Fragment implements NavigationBarView. OnItemSelectedListener {
    public NavFragment() {
        super(R.layout.fragment_nav);
    }
    BottomNavigationView nav;
    Context context;
    View view;

    @Override
    public void onViewCreated(@NonNull View createdView, Bundle savedInstanceState) {
        context = getContext();
        view = createdView;
        nav = createdView.findViewById(R.id.bottomNavigationView);

        //Check if the activity is instance of one of the two, and if so set it to be active item.
//        if (getActivity() instanceof MainActivity) {
//            nav.setSelectedItemId(R.id.accNav);
//            Log.i("TAG", "SETTING TO ACC");
//        } else if (getActivity() instanceof RotationActivity) {
//            nav.setSelectedItemId(R.id.compassNav);
//            Log.i("TAG", "SETTING TO COMPASS");
//        }

        //Set the listener AFTER the active item is changed
        nav.setOnItemSelectedListener(this);
    }

    @Override
    public boolean
    onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
//        if (id == R.id.accNav) {
//            this.startActivity(new Intent(context, MainActivity.class));
//            return true;
//        } else if (id == R.id.compassNav) {
//            this.startActivity(new Intent(context, RotationActivity.class));
//            return true;
//        }
        return false;
    }
}