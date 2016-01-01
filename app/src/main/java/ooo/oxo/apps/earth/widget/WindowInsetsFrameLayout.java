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

package ooo.oxo.apps.earth.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class WindowInsetsFrameLayout extends FrameLayout {

    private static final String TAG = "WindowInsetsFrameLayout";

    public WindowInsetsFrameLayout(Context context) {
        this(context, null);
    }

    public WindowInsetsFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WindowInsetsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            if (!insets.hasSystemWindowInsets()) {
                return insets;
            }

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (ViewCompat.getFitsSystemWindows(child)) {
                    Rect rect = new Rect(
                            insets.getSystemWindowInsetLeft(),
                            insets.getSystemWindowInsetTop(),
                            insets.getSystemWindowInsetRight(),
                            insets.getSystemWindowInsetBottom());

                    LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    int gravity = GravityCompat.getAbsoluteGravity(
                            lp.gravity, ViewCompat.getLayoutDirection(child));

                    if (lp.width != LayoutParams.MATCH_PARENT) {
                        if ((gravity & Gravity.LEFT) != Gravity.LEFT) {
                            rect.left = 0;
                        }

                        if ((gravity & Gravity.RIGHT) != Gravity.RIGHT) {
                            rect.right = 0;
                        }
                    }

                    if (lp.height != LayoutParams.MATCH_PARENT) {
                        if ((gravity & Gravity.TOP) != Gravity.TOP) {
                            rect.top = 0;
                        }

                        if ((gravity & Gravity.BOTTOM) != Gravity.BOTTOM) {
                            rect.bottom = 0;
                        }
                    }

                    ViewCompat.dispatchApplyWindowInsets(
                            child, insets.replaceSystemWindowInsets(rect));
                }
            }

            return insets.consumeSystemWindowInsets();
        });
    }

}
