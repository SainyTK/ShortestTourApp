package com.shortesttour.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.shortesttour.R;
import com.shortesttour.ui.search.PlaceAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Fragment1 extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.from(getContext()).inflate(R.layout.fragment_test,container,false);
        ButterKnife.bind(this,root);

        String[] strings = getActivity().getResources().getStringArray(R.array.countries_array);
        PlaceAdapter adapter = new PlaceAdapter(getContext(),strings);
        recyclerView.setAdapter(adapter);

        return root;
    }

}
