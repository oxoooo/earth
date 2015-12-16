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

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ooo.oxo.apps.earth.databinding.AboutActivityBinding;
import ooo.oxo.apps.earth.databinding.AboutHeaderBinding;
import ooo.oxo.apps.earth.databinding.AboutLibraryItemBinding;
import ooo.oxo.library.databinding.support.widget.BindingRecyclerView;

public class AboutActivity extends AppCompatActivity {

    private final ArrayMap<String, String> libraries = new ArrayMap<>();

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AboutActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.about_activity);

        binding.toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        libraries.put("bumptech / glide", "https://github.com/bumptech/glide");

        binding.libraries.setAdapter(new LibrariesAdapter());
    }

    private void open(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    class LibrariesAdapter extends RecyclerView.Adapter<BindingRecyclerView.ViewHolder> {

        private final LayoutInflater inflater = getLayoutInflater();

        @Override
        public BindingRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case 0:
                    return new HeaderViewHolder(parent);
                case 1:
                    return new ItemViewHolder(parent);
                case 2:
                    return new QrcodeViewHolder(parent);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(BindingRecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    onBindHeaderViewHolder((HeaderViewHolder) holder, position);
                    break;
                case 1:
                    onBindItemViewHolder((ItemViewHolder) holder, position);
            }
        }

        private void onBindHeaderViewHolder(HeaderViewHolder holder, int position) {
            switch (position) {
                case 0:
                    holder.binding.setName(R.string.fork_me_on_github);
                    break;
                case 2:
                    holder.binding.setName(R.string.follow_us_on_wechat);
                    break;
                case 4:
                    holder.binding.setName(R.string.images_from);
                    break;
                case 6:
                    holder.binding.setName(R.string.inspired_by);
                    break;
                case 8:
                    holder.binding.setName(R.string.libraries_used);
                    break;
            }
        }

        private void onBindItemViewHolder(ItemViewHolder holder, int position) {
            if (position == 1) {
                holder.binding.setName("oxoooo / earth");
            } else if (position == 5) {
                holder.binding.setName("himawari8.nict.go.jp");
            } else if (position == 7) {
                holder.binding.setName("bitdust / EarthLiveSharp");
            } else if (position >= 9) {
                holder.binding.setName(libraries.keyAt(position - 9));
            }
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                case 2:
                case 4:
                case 6:
                case 8:
                    return 0;
                case 3:
                    return 2;
                default:
                    return 1;
            }
        }

        @Override
        public int getItemCount() {
            return libraries.size() + 9;
        }

        private void handleItemClick(int position) {
            if (position == 1) {
                open("https://github.com/oxoooo/earth");
            } else if (position == 5) {
                open("http://himawari8.nict.go.jp");
            } else if (position == 7) {
                open("https://github.com/bitdust/EarthLiveSharp");
            } else if (position >= 9) {
                open(libraries.valueAt(position - 9));
            }
        }

        class HeaderViewHolder extends BindingRecyclerView.ViewHolder<AboutHeaderBinding> {

            public HeaderViewHolder(ViewGroup parent) {
                super(inflater, R.layout.about_header, parent);
            }

        }

        class ItemViewHolder extends BindingRecyclerView.ViewHolder<AboutLibraryItemBinding> {

            public ItemViewHolder(ViewGroup parent) {
                super(inflater, R.layout.about_library_item, parent);
                itemView.setOnClickListener(v -> handleItemClick(getAdapterPosition()));
            }

        }

        class QrcodeViewHolder extends BindingRecyclerView.ViewHolder<AboutLibraryItemBinding> {

            public QrcodeViewHolder(ViewGroup parent) {
                super(inflater, R.layout.about_qrcode, parent);
                itemView.setOnClickListener(v -> handleItemClick(getAdapterPosition()));
            }

        }

    }

}
