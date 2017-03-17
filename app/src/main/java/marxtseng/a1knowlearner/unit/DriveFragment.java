package marxtseng.a1knowlearner.unit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import marxtseng.a1knowlearner.R;

/**
 * Created by marx on 06/02/2017.
 */

public class DriveFragment extends UnitFragment {

    private View mView;

    private MenuItem mDoneMenuItem;

    public DriveFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_unit_drive, container, false);

        setUnit(getArguments().getString("UQID"));

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mDoneMenuItem = menu.findItem(R.id.action_send);
        mDoneMenuItem.setVisible(false);
    }
}
