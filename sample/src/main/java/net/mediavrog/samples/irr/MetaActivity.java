package net.mediavrog.samples.irr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MetaActivity extends AppCompatActivity {

    private final static int[][] sSocialLinks = new int[][]{
            new int[]{R.string.repository, R.drawable.ic_social_github, R.string.link_github},
            new int[]{R.string.my_other_apps, R.drawable.ic_social_google_play, R.string.link_publisher},
            new int[]{R.string.homepage, R.drawable.ic_social_news, R.string.link_homepage}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meta);

        // version
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        // links
        findViewById(R.id.publisher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_publisher))));
            }
        });
        ((TextView) findViewById(R.id.thanks)).setText(Html.fromHtml(getString(R.string.thanks)));

        ViewGroup socialWrapper = (ViewGroup) findViewById(R.id.social);
        LayoutInflater inflater = getLayoutInflater();
        for (int[] socialLinkInfo : sSocialLinks) {
            ViewGroup socialLink = (ViewGroup) inflater.inflate(R.layout.social_link, socialWrapper, false);
            ((ImageView) socialLink.findViewById(R.id.social_icon)).setImageResource(socialLinkInfo[1]);
            ((TextView) socialLink.findViewById(R.id.social_text)).setText(socialLinkInfo[0]);
            socialLink.setTag(getString(socialLinkInfo[2]));
            socialLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse((String) v.getTag())));
                }
            });

            socialWrapper.addView(socialLink);
        }
    }
}
