package com.cremamobile.filemanager.settingview;

import com.cremamobile.filemanager.R;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class SlideAnimationLayout extends LinearLayout
{
	Context mContext;
    private Animation inAnimation;
    private Animation outAnimation;

    public SlideAnimationLayout(Context context) {
        super(context);
        
        mContext = context;
        createAnimation();
    }

    public void createAnimation() {
        this.inAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
        this.outAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
    }
    
    @Override
    public void setVisibility(int visibility) {
        if (getVisibility() != visibility) {
            if (visibility == VISIBLE) {
                if (inAnimation != null) startAnimation(inAnimation);
            }
            else if ((visibility == INVISIBLE) || (visibility == GONE)) {
                if (outAnimation != null) startAnimation(outAnimation);
            }
        }

        super.setVisibility(visibility);
    }
}