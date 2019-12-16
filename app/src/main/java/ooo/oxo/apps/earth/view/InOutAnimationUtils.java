/*
 * Mantou Earth - Live your wallpaper with live earth
 * Copyright (C) 2015-2019 XiNGRZ <xxx@oxo.ooo>
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

package ooo.oxo.apps.earth.view;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.AnimRes;

public class InOutAnimationUtils {

    public static void animateIn(View v, @AnimRes int anim) {
        Animation animation = AnimationUtils.loadAnimation(v.getContext(), anim);

        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }
        });

        v.startAnimation(animation);
    }

    public static void animateOut(View v, @AnimRes int anim) {
        Animation animation = AnimationUtils.loadAnimation(v.getContext(), anim);

        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
            }
        });

        v.startAnimation(animation);
    }

    public static class AnimationListenerAdapter implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }

}
