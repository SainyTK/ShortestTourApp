package com.shortesttour.ui.select_algoritm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.shortesttour.R;
import com.shortesttour.utils.BundleStore;
import com.shortesttour.utils.PrefsUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectAlgorithmFragment extends Fragment {

    @BindView(R.id.radio_nnb)
    RadioButton radioNnb;
    @BindView(R.id.radio_dp)
    RadioButton radioDp;

    private ChangeAlgorithmListener mListener;

    public interface ChangeAlgorithmListener {
        void onChangeAlgorithm();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_select_algorithm, container, false);
        ButterKnife.bind(this, root);
        update();
        return root;
    }

    public void setListener(ChangeAlgorithmListener listener) {
        mListener = listener;
    }

    public void update(int numPlace) {
        try{
            if (numPlace < Integer.parseInt(getContext().getString(R.string.dp_limit))) {
                radioDp.setEnabled(true);
            } else {
                radioDp.setEnabled(false);
                radioNnb.setChecked(true);
            }
        }
        catch(NullPointerException ne){
            Log.e("error", "update: ",ne );
        }

    }

    public void update() {
        int selected = PrefsUtil.getAlgorithm(getContext());
        switch (selected) {
            case PrefsUtil.NEAREST_NEIGHBOR:
                radioNnb.setChecked(true);
                break;
            case PrefsUtil.DYNAMIC_PROGRAMMING:
                radioDp.setChecked(true);
                break;
        }
    }

    @OnClick(R.id.radio_dp)
    void selectDynamicProgramming() {
        radioDp.setChecked(true);
        PrefsUtil.setAlgorithm(getContext(), PrefsUtil.DYNAMIC_PROGRAMMING);
        if (mListener != null)
            mListener.onChangeAlgorithm();
    }

    @OnClick(R.id.radio_nnb)
    void selectNearestNeighbor() {
        radioNnb.setChecked(true);
        PrefsUtil.setAlgorithm(getContext(), PrefsUtil.NEAREST_NEIGHBOR);
        if (mListener != null)
            mListener.onChangeAlgorithm();
    }
}
