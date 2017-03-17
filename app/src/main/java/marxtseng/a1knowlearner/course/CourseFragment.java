package marxtseng.a1knowlearner.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import marxtseng.a1knowlearner.NavigationActivity;
import marxtseng.a1knowlearner.R;
import marxtseng.a1knowlearner.common.JsonArrayRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;

/**
 * Created by marx on 06/02/2017.
 */

public class CourseFragment extends Fragment {

    private String TAG = "CourseFragment";

    private View mView;
    private JSONArray mItems;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    public CourseFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_course, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startup();
            }
        });

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        startup();

        return mView;
    }

    private void startup() {
        Utility.GetCourses(getActivity(), new JsonArrayRequestCallBack() {
            @Override
            public void OnSuccess(JSONArray response) {
                mItems = response;

                try {
                    ArrayList<Map<String, String>> items = new ArrayList<>();

                    for (int i = 0; i < mItems.length(); i++) {
                        Map<String, String> item = new HashMap<>();
                        item.put("uqid", mItems.getJSONObject(i).getString("uqid"));
                        item.put("name", mItems.getJSONObject(i).getString("name"));
                        item.put("done_units", mItems.getJSONObject(i).getString("done_units"));
                        item.put("total_units", mItems.getJSONObject(i).getString("total_units"));

                        items.add(item);
                    }

                    mRecyclerView.setAdapter(new CourseRecyclerViewAdapter(items));
                    mSwipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }

            @Override
            public void OnError(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    public class CourseRecyclerViewAdapter
            extends RecyclerView.Adapter<CourseRecyclerViewAdapter.ViewHolder> {

        private String TAG = "CourseRecyclerViewAdapter";
        private final ArrayList<Map<String, String>> mValues;

        public CourseRecyclerViewAdapter(ArrayList<Map<String, String>> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_course_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mNameView.setText(holder.mItem.get("name"));
            holder.mUnitsView.setText(holder.mItem.get("done_units") + " / " + holder.mItem.get("total_units"));
            Utility.SetImageView(holder.mLogoView, "course", holder.mItem.get("uqid"), true, 8);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "course");
                    bundle.putString("uqid", holder.mItem.get("uqid"));
                    bundle.putString("name", holder.mItem.get("name"));
                    bundle.putString("done_units", holder.mItem.get("done_units"));
                    bundle.putString("total_units", holder.mItem.get("total_units"));

                    Intent intent = new Intent(v.getContext(), NavigationActivity.class);
                    intent.putExtra("COURSE_TASK_ITEM", bundle);
                    getActivity().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public Map<String, String> mItem;
            public final View mView;
            public final ImageView mLogoView;
            public final TextView mNameView;
            public final TextView mUnitsView;

            public ViewHolder(View view) {
                super(view);

                mView = view;
                mLogoView = (ImageView) view.findViewById(R.id.course_logo);
                mNameView = (TextView) view.findViewById(R.id.course_name);
                mUnitsView = (TextView) view.findViewById(R.id.course_units);
            }
        }

    }
}
