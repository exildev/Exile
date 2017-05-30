package co.com.exile.exile.profile;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.liuguangqiang.ipicker.IPicker;

import java.io.File;
import java.util.List;

import co.com.exile.exile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        FloatingActionButton picPhoto = (FloatingActionButton) rootView.findViewById(R.id.pic_photo);
        final ImageView profile = (ImageView) rootView.findViewById(R.id.profile);

        picPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPicker.setLimit(1);
                IPicker.open(getContext());
                IPicker.setOnSelectedListener(new IPicker.OnSelectedListener() {
                    @Override
                    public void onSelected(List<String> paths) {
                        if (paths.size() > 0) {
                            profile.setImageURI(Uri.fromFile(new File(paths.get(0))));
                        }
                    }
                });
            }
        });

        View helpButton = rootView.findViewById(R.id.help_button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), HelpActivity.class);
                startActivity(intent);
            }
        });

        View accountButton = rootView.findViewById(R.id.account_button);
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AccountActivity.class);
                startActivity(intent);
            }
        });


        View notificationButton = rootView.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NotificationActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

}
