package net.mediavrog.samples.irr;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.mediavrog.irr.DefaultRuleEngine;

public class MainActivity extends AppCompatActivity {
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
                {"Custom Rule Engine Demo", "Create your own fancy rules backed by any system."},
                {"Irr List Decorator Demo", "Decorate your ListAdapter to automatically inject the IRR Element."},
                {"Irr Async List Decorator Demo", "Decorate your ListAdapter with data loaded asynchronously."}
        };

        clazzes = new Class[]{
                DefaultEngineActivity.class,
                DefaultEngineSnackBarActivity.class,
                CustomEngineActivity.class,
                DefaultEngineListActivity.class,
                DefaultEngineAsyncListActivity.class
        };

        ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setAdapter(new ArrayAdapter<String[]>(this, android.R.layout.simple_list_item_2, android.R.id.text1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(getItem(position)[0]);
                text2.setText(getItem(position)[1]);
                text2.setPadding(0, 0, 0, 20);
                text2.setTextColor(Color.GRAY);
                return view;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchActivity(clazzes[position]);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_contact:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_help))));
                break;
            case R.id.action_about:
                launchActivity(MetaActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    void launchActivity(Class clazz) {
        startActivity(new Intent(this, clazz));
    }
}
