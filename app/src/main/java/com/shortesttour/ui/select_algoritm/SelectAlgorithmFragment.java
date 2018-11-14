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
import android.widget.RadioGroup;

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
    @BindView(R.id.radio)
    RadioGroup radioGroup;

    private int prevAlgorithm;
    private ChangeAlgorithmListener mListener;

    public interface ChangeAlgorithmListener {
        void onChangeAlgorithm();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View root = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_select_algorithm, container, false);
            ButterKnife.bind(this, root);

            return root;
        } catch (Exception e) {
            Log.e("error", "onCreateView: ", e);
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void setListener(ChangeAlgorithmListener listener) {
        mListener = listener;
    }

    public void update() {
        Log.d("test", "update: call fn");
        int selected = PrefsUtil.getAlgorithm(getContext());
        prevAlgorithm = selected;
        switch (selected) {
            case PrefsUtil.NEAREST_NEIGHBOR:
                Log.d("test", "update: nn");
                radioGroup.clearCheck();
                radioNnb.setChecked(true);
                break;
            case PrefsUtil.DYNAMIC_PROGRAMMING:
                Log.d("test", "update: dp");
                radioGroup.clearCheck();
                radioDp.setChecked(true);
                break;
        }
    }

    @OnClick(R.id.radio_dp)
    void selectDynamicProgramming() {
        if(prevAlgorithm!=PrefsUtil.DYNAMIC_PROGRAMMING) {
            radioDp.setChecked(true);
            PrefsUtil.setAlgorithm(getContext(), PrefsUtil.DYNAMIC_PROGRAMMING);
            prevAlgorithm = PrefsUtil.DYNAMIC_PROGRAMMING;
            if (mListener != null)
                mListener.onChangeAlgorithm();
        }
    }

    @OnClick(R.id.radio_nnb)
    void selectNearestNeighbor() {
        if(prevAlgorithm!=PrefsUtil.NEAREST_NEIGHBOR){
            radioNnb.setChecked(true);
            PrefsUtil.setAlgorithm(getContext(), PrefsUtil.NEAREST_NEIGHBOR);
            prevAlgorithm = PrefsUtil.NEAREST_NEIGHBOR;
            if (mListener != null)
                mListener.onChangeAlgorithm();
        }
    }


}
