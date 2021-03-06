package fhws.marcelgross.incoming.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fhws.marcelgross.incoming.Adapter.DBAdapter;
import fhws.marcelgross.incoming.Adapter.NetworkChangeReceiver;
import fhws.marcelgross.incoming.Adapter.NewsAdapter;
import fhws.marcelgross.incoming.Objects.NewsObject;
import fhws.marcelgross.incoming.R;
import fhws.marcelgross.incoming.UrlHandler;
import fhws.marcelgross.incoming.Volley.AppController;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {


    private TextView noNews;
    private DBAdapter db;
    private View view;
    private  RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_news, container, false);
        db = new DBAdapter(getActivity());
        noNews = (TextView) view.findViewById(R.id.noNews);
        if ( NetworkChangeReceiver.getInstance().isConnected){
            loadData();
        }
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        setUpView(db.getAllNews());


        return view;
    }

    public void setUpView(ArrayList<NewsObject> newsObjects){
        if (newsObjects.isEmpty()){
            noNews.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            noNews.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(llm);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            NewsAdapter mNewsAdapter = new NewsAdapter(newsObjects, R.layout.news_card, getActivity(), getFragmentManager());
            mRecyclerView.setAdapter(mNewsAdapter);
            mNewsAdapter.notifyDataSetChanged();
        }
    }

    private void loadData(){
        final Genson genson = new Genson();
        JsonArrayRequest req = new JsonArrayRequest(UrlHandler.NEWSURL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<NewsObject> objects;
                        objects = genson.deserialize(response.toString(), new GenericType<ArrayList<NewsObject>>() {});
                        checkViewReaload(objects);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("result", "Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        AppController.getInstance(getActivity()).addToRequestQueue(req, "news_request");
    }

    private void checkViewReaload(ArrayList<NewsObject> newsObjects){
        int counter = 0;
        ArrayList<Long> ids = db.getAllNewsIDs();
        for (int i = 0; i<newsObjects.size(); i++){
            if (!ids.contains(newsObjects.get(i).getId()))
                ++counter;
        }
        db.saveNews(newsObjects);
        if (counter>0){
            setUpView(db.getAllNews());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_info);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_info) {
            startDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startDialog() {
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setView(LayoutInflater.from(getActivity()).inflate(R.layout.dialog, null)).setTitle(R.string.action_info).create();
        dialog.show();
    }

}
