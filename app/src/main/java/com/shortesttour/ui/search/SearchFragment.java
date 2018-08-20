package com.shortesttour.ui.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.shortesttour.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    @BindView(R.id.recycler_view_place_list)
    RecyclerView recyclerView;

    PlaceExpandableAdapter adapter;

    public static SearchFragment newInstance(){
        SearchFragment searchFragment = new SearchFragment();
        return searchFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_search,container,false);
        ButterKnife.bind(this,root);

        String[] places = getContext().getResources().getStringArray(R.array.countries_array);
        List<PlaceTitle> placeTitleList = createPlaceTitleList(places);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PlaceExpandableAdapter(getContext(),placeTitleList);
        adapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onParentExpanded(int parentPosition) {
                adapter.collapseAllParents();;
                adapter.expandParent(parentPosition);
            }

            @Override
            public void onParentCollapsed(int parentPosition) {

            }
        });

        recyclerView.setAdapter(adapter);

        return root;
    }

    private List<PlaceTitle> createPlaceTitleList(String[] data){
        List<PlaceTitle> placeTitles = new ArrayList<>();
        for(int i=0 ; i<data.length ; i++){
            PlaceTitle title = new PlaceTitle(data[i]);
            placeTitles.add(title);
        }
        return placeTitles;
    }

    public void setSearchDataSet(ArrayList<String> dataSet){
        String[] strings = new String[dataSet.size()];
        dataSet.toArray(strings);

    }


}
