package net.mediavrog.samples.irr;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.mediavrog.irr.DefaultRuleEngine;

public class MainActivity extends ListActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private Class[] clazzes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // simulate an app start; this should really go into a custom Application#onStart
        DefaultRuleEngine.trackAppStart(this);

        setContentView(R.layout.activity_main);

        // Defined Array values to show in ListView
        String[][] values = new String[][]{
                {"Default Rule Engine Demo", "All configuration is done in xml; easiest to integrate"},
                {"SnackBar Design Demo", "Shows the flexibility of IRR to customize to your design."},
                {"Custom Rule Engine Demo", "Create your own fancy rules backed by any system."}
        };

        clazzes = new Class[]{
                DefaultRuleEngineActivity.class,
                SnackBarDesignActivity.class,
                CustomRuleEngineActivity.class
        };

        setListAdapter(new ArrayAdapter<String[]>(this, android.R.layout.simple_list_item_2, android.R.id.text1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(getItem(position)[0]);
                text2.setText(getItem(position)[1]);
                return view;
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "Clicked list at position " + position);
        launchActivity(clazzes[position]);
    }

    void launchActivity(Class clazz) {
        startActivity(new Intent(this, clazz));
    }
}
