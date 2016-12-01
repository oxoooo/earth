/*
 * Mantou Earth - Live your wallpaper with live earth
 * Copyright (C) 2015  XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.apps.earth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import ooo.oxo.apps.earth.databinding.AboutActivityBinding;

public class AboutActivity extends AppCompatActivity {

    private ClipboardManager cm;

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AboutActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.about_activity);

        binding.toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        final String template = getTemplate()
                .replace("{{fork_me_on_github}}", getString(R.string.fork_me_on_github))
                .replace("{{sponsor}}", getString(R.string.sponsor))
                .replace("{{sponsor_text}}", getString(R.string.sponsor_text))
                .replace("{{contact_us}}", getString(R.string.contact_us))
                .replace("{{contact_us_text}}", getString(R.string.contact_us_text))
                .replace("{{images_from}}", getString(R.string.images_from))
                .replace("{{inspired_by}}", getString(R.string.inspired_by))
                .replace("{{libraries_used}}", getString(R.string.libraries_used));

        binding.chrome.setWebViewClient(new AboutClient());
        binding.chrome.loadDataWithBaseURL("file:///", template, "text/html; charset=utf-8", null, null);

        cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    private String getTemplate() {
        try {
            return IOUtils.toString(getResources().openRawResource(R.raw.about), "UTF-8");
        } catch (IOException e) {
            return "";
        }
    }

    private void copy(String text) {
        cm.setPrimaryClip(ClipData.newPlainText(text, text));
        Toast.makeText(this, getString(R.string.about_copied, text), Toast.LENGTH_SHORT).show();
    }

    private void open(Uri uri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    private class AboutClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            if ("copy".equals(uri.getScheme())) {
                copy(uri.getSchemeSpecificPart());
                return true;
            } else {
                open(uri);
                return true;
            }
        }

    }

}
