package com.nmm.plugin.action.ui;

import java.awt.*;
import java.math.BigDecimal;

public class UIUtil {
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * 根据大小，计算中心位置
     * @param width
     * @param height
     * @return
     */
    public static Point calcuCenterPoint(int width,int height) {
        int x1 = new BigDecimal((screenSize.getWidth() - width)/2).intValue();
        int y1 = new BigDecimal((screenSize.getHeight() - height)/2).intValue();
        return new Point(x1,y1);
    }
}
