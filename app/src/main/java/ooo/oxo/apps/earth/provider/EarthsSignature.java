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

package ooo.oxo.apps.earth.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.bumptech.glide.load.Key;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import ooo.oxo.apps.earth.dao.Earth;

public class EarthsSignature implements Key {

    private final long fetchedAt;

    public EarthsSignature(ContentResolver resolver, Uri uri) {
        if (!EarthsContract.CONTENT_TYPE_ITEM.equals(resolver.getType(uri))) {
            fetchedAt = 0;
            return;
        }

        Cursor cursor = resolver.query(uri, null, null, null, null);

        if (cursor == null) {
            fetchedAt = 0;
            return;
        }

        Earth earth = Earth.fromCursor(cursor);

        cursor.close();

        if (earth == null) {
            fetchedAt = 0;
            return;
        }

        fetchedAt = earth.fetchedAt.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EarthsSignature that = (EarthsSignature) o;

        return fetchedAt == that.fetchedAt;
    }

    @Override
    public int hashCode() {
        return 22 * (int) (fetchedAt ^ (fetchedAt >>> 32));
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) throws UnsupportedEncodingException {
        byte[] data = ByteBuffer.allocate(8).putLong(fetchedAt).array();
        messageDigest.update(data);
    }

}
