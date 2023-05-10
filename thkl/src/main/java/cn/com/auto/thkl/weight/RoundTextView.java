package cn.com.auto.thkl.weight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class RoundTextView extends AppCompatTextView {

    private Paint backgroundPaint;

    public RoundTextView(Context context) {
        this(context, null);
    }

    public RoundTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(android.R.color.white));
        backgroundPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int radius = Math.min(getWidth(), getHeight()) / 2;
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, backgroundPaint);
        super.onDraw(canvas);
    }
}
