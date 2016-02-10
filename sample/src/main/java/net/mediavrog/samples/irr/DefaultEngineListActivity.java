package net.mediavrog.samples.irr;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.mediavrog.irr.DefaultRuleEngine;
import net.mediavrog.irr.IrrAdapterDecorator;

public class DefaultEngineListActivity extends DefaultEngineActivity {
    public static final String TAG = DefaultEngineListActivity.class.getSimpleName();

    protected int getLayoutResId() {
        return R.layout.activity_default_engine_list;
    }

    protected void initialize() {
        // setup listview with 100 elements
        String[] values = new String[100];
        int i = 0;
        for (; i < 100; i++) values[i] = "A fancy list item #" + (i + 1);

        ListView lv = (ListView) findViewById(android.R.id.list);
        ArrayAdapter myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);

        // show the irr view at the 10th position (index starts at 0)
        // don't pass a rule engine here but use the default implementation provided by the irr itself
        IrrAdapterDecorator irrDecoAdapter = new IrrAdapterDecorator(this, myAdapter, 9, R.layout.standard_irr_layout);
        lv.setAdapter(irrDecoAdapter);
        engine = (DefaultRuleEngine) irrDecoAdapter.getUnderlyingRuleEngine();
    }
}
