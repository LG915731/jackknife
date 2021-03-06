/*
 * Copyright (C) 2018 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.widget.popupdialog;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.lwh.jackknife.widget.R;

public class PopupDialog {

    protected AbstractDialogView mDialogView;
    protected FrameLayout mDecorView;
    protected FrameLayout mContentView;
    protected View mDialogRoot;
    protected View mDialogContent;
    private Animation mPushOutAnim;
    private Animation mPushInAnim;
    private boolean mDismissing;
    private Activity mOwnActivity;
    private LayoutInflater mInflater;
    private boolean mHasShown;
    private OnAttachListener mOnAttachListener;
    private OnDismissListener mOnDismissListener;

    protected PopupDialog(DialogView dialogView) {
        this.mDialogView = dialogView;
    }

    public Activity getOwnActivity() {
        return mOwnActivity;
    }

    protected PopupDialog(Builder builder) {
        this.mOwnActivity = (Activity) builder.getContext();
        this.mInflater = LayoutInflater.from(mOwnActivity);
        this.mDecorView = (FrameLayout) mOwnActivity.getWindow().getDecorView();
        this.mContentView = (FrameLayout) mDecorView.findViewById(android.R.id.content);
        applyAnimation(builder);
        this.mDialogView = builder.dialogView;
        this.mDialogView.setGravity(builder.gravity);
        this.mDialogRoot = mDialogView.performInflateView(mInflater, mDecorView);
        this.mDialogRoot.setLayoutParams(mDialogView.getShadowLayoutParams());
        this.mDialogContent = mDialogView.getContentView();
    }

    public interface OnAttachListener {
        void onAttached(View root);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    protected void applyAnimation(Builder builder) {
        mPushOutAnim = builder.getPushOutAnimation();
        mPushInAnim = builder.getPushInAnimation();
    }

    public void show() {
        onAttached(mDialogRoot);
        mHasShown = true;
    }

    public boolean isShown() {
        return mHasShown;
    }

    public void toggle() {
        if (isShown()) {
            dismiss();
        } else {
            show();
        }
    }

    public void dismiss() {
        if (mDismissing) {
            return;
        }
        mPushOutAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDecorView.post(new Runnable() {
                    @Override
                    public void run() {
                        mDecorView.removeView(mDialogRoot);
                        mDismissing = false;
                        mHasShown = false;
                        if (mOnDismissListener != null) {
                            mOnDismissListener.onDismiss();
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mDialogContent.startAnimation(mPushOutAnim);
        mDismissing = true;
    }

    private void onAttached(View viewRoot) {
        mDecorView.addView(viewRoot);
        mDialogContent.startAnimation(mPushInAnim);
        viewRoot.requestFocus();
        mDialogView.setOnBackListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getAction()) {
                    case KeyEvent.ACTION_UP:
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dismiss();
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mDialogView.setOnCancelListener(new AbstractDialogView.OnCancelListener() {
            @Override
            public void onCancel() {
                dismiss();
            }
        });
        if (mOnAttachListener != null) {
            mOnAttachListener.onAttached(mDialogRoot);
        }
    }

    public static class Builder {

        protected static final int INVALID = -1;
        protected static final int INVALID_COLOR = 0;
        /* @hide */
        private Animation pushInAnim;
        /* @hide */
        private Animation pushOutAnim;
        private Context context;
        private int gravity;
        protected AbstractDialogView dialogView;

        public Builder(Context context) {
            if (!Activity.class.isAssignableFrom(context.getClass())) {
                throw new IllegalArgumentException("need activity context");
            }
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        private Animation getPushInAnimation() {
            return (pushInAnim == null) ? AnimationUtils.loadAnimation(context,
                    R.anim.jknf_bottom_in) : pushInAnim;
        }

        public Builder setPushInAnimation(int animResId) {
            this.pushInAnim = AnimationUtils.loadAnimation(context, animResId);
            return this;
        }

        public Builder setPushInAnimation(Animation in) {
            this.pushInAnim = in;
            return this;
        }

        private Animation getPushOutAnimation() {
            return (pushOutAnim == null) ? AnimationUtils.loadAnimation(context,
                    R.anim.jknf_bottom_out) : pushOutAnim;
        }

        public Builder setPushOutAnimation(int animResId) {
            this.pushOutAnim = AnimationUtils.loadAnimation(context, animResId);
            return this;
        }

        public Builder setPushOutAnimation(Animation out) {
            this.pushOutAnim = out;
            return this;
        }

        public Builder setDialogView(DialogView dialogView) {
            this.dialogView = dialogView;
            return this;
        }

        public Builder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public PopupDialog create() {
            return new PopupDialog(this);
        }
    }

    public void setOnAttachListener(OnAttachListener l) {
        this.mOnAttachListener = l;
    }

    public void setOnDismissListener(OnDismissListener l) {
        this.mOnDismissListener = l;
    }

    public LayoutInflater getLayoutInflater() {
        return mInflater;
    }
}

