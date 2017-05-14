package com.datetime;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyPickerDialog extends Dialog {

    private Params params;

    public MyPickerDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    private void setParams(MyPickerDialog.Params params) {
        this.params = params;
    }


    public void setSelection(String itemValue) {
        if (params.dataList.size() > 0) {
            int idx = params.dataList.indexOf(itemValue);
            if (idx >= 0) {
                params.initSelection = idx;
                params.loopData.setCurrentItem(params.initSelection);
            }
        }
    }

    public interface OnDataSelectedListener {
        void onDataSelected(Builder.Bean bean);
        void onCancel();
    }

    private static final class Params {
        private boolean shadow = true;
        private boolean canCancel = true;
        private LoopView loopData, loopHour, loopMin;
        private String title;
        private int initSelection, hour, min;
        private OnDataSelectedListener callback;
        private final List<String> dataList = new ArrayList<>();
    }

    public static class Builder {
        private final Context context;
        private final MyPickerDialog.Params params;

        public Builder(Context context) {
            this.context = context;
            params = new MyPickerDialog.Params();
        }

        private final Bean getCurrDateValue() {
            Bean bean = new Bean(params.loopData.getCurrentItem(),
                    params.loopHour.getCurrentItem(), params.loopMin.getCurrentItem());
            return bean;
        }

        public Builder setData(List<String> dataList) {
            params.dataList.clear();
            params.dataList.addAll(dataList);
            return this;
        }

        public Builder setTitle(String title) {
            params.title = title;
            return this;
        }

        public Builder setSelection(int selection) {
            params.initSelection = selection;
            return this;
        }

        public Builder setHour(int hour) {
            params.hour = hour < 0 ? 0 : hour > 23 ? 23 : hour;
            return this;
        }

        public Builder setMin(int min) {
            params.min = min < 0 ? 0 : min > 59 ? 59 : min;
            return this;
        }

        public Builder setOnDataSelectedListener(OnDataSelectedListener onDataSelectedListener) {
            params.callback = onDataSelectedListener;
            return this;
        }

        /**
         * 获取当前选择的时间
         *
         * @return int[]数组形式返回。例[12,30]
         */
        private final int[] getCurrDateValues() {
            int currHour = Integer.parseInt(params.loopHour.getCurrentItemValue());
            int currMin = Integer.parseInt(params.loopMin.getCurrentItemValue());
            return new int[]{currHour, currMin};
        }

        public MyPickerDialog create() {
            final MyPickerDialog dialog = new MyPickerDialog(context, params.shadow ? R.style.Theme_Light_NoTitle_Dialog : R.style.Theme_Light_NoTitle_NoShadow_Dialog);
            View view = LayoutInflater.from(context).inflate(R.layout.layout_picker_data, null);

            if (!TextUtils.isEmpty(params.title)) {
                TextView txTitle = (TextView) view.findViewById(R.id.tx_title);
                txTitle.setText(params.title);
                txTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        params.callback.onCancel();
                    }
                });
            }
//            if (!TextUtils.isEmpty(params.unit)) {
//                TextView txUnit = (TextView) view.findViewById(R.id.tx_unit);
//                txUnit.setText(params.unit);
//            }

            final LoopView loopData = (LoopView) view.findViewById(R.id.loop_data);
            loopData.setArrayList(params.dataList);
            loopData.setNotLoop();
            loopData.setTextSize(22f);
            if (params.dataList.size() > 0) loopData.setCurrentItem(params.initSelection);

            final LoopView loopHour = (LoopView) view.findViewById(R.id.loop_hour);

            //修改优化边界值 by lmt 16/ 9 /12.禁用循环滑动,循环滑动有bug
            loopHour.setCyclic(false);
            loopHour.setArrayList(DateUtil.d(0, 24));
            loopHour.setCurrentItem(params.hour);
//            loopHour.setTextSize(16f);

            final LoopView loopMin = (LoopView) view.findViewById(R.id.loop_min);
            loopMin.setCyclic(false);
            loopMin.setArrayList(DateUtil.d(0, 60));
            loopMin.setCurrentItem(params.min);
//            loopMin.setTextSize(16f);

            view.findViewById(R.id.tx_finish).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    params.callback.onDataSelected(getCurrDateValue());
                }
            });

            Window win = dialog.getWindow();
            win.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            win.setAttributes(lp);
            win.setGravity(Gravity.BOTTOM);
            win.setWindowAnimations(R.style.Animation_Bottom_Rising);

            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(params.canCancel);
            dialog.setCancelable(params.canCancel);

            params.loopData = loopData;
            params.loopHour = loopHour;
            params.loopMin = loopMin;
            dialog.setParams(params);

            return dialog;
        }

        public static class Bean {
            int position;
            int hour;
            int minu;

            public Bean(int position, int hour, int minu)
            {
                this.position = position;
                this.hour = hour;
                this.minu = minu;
            }

            public int getPosition()
            {
                return position;
            }

            public int getHour()
            {
                return hour;
            }

            public int getMinu()
            {
                return minu;
            }

            public String getHourStr()
            {
                return hour < 10 ? ("0" + hour) : ("" + hour);
            }

            public String getMinuStr()
            {
                return minu < 10 ? ("0" + minu) : ("" + minu);
            }

            public void setPosition(int position)
            {
                this.position = position;
            }

            public void setHour(int hour)
            {
                this.hour = hour;
            }

            public void setMinu(int minu)
            {
                this.minu = minu;
            }
        }
    }
}
