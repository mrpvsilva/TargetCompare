package com.som.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import javax.swing.JFrame;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ScatterAdd extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String TITLE = "GRÁFICO";
    private final double[] xe;
    private final double[] ye;
    private final double[] xs;
    private final double[] ys;

    public ScatterAdd(String s, double[] xe, double[] ye, double[] xs, double[] ys) {
        super(s);
        this.xe = xe;
        this.ye = ye;
        this.xs = xs;
        this.ys = ys;

        final ChartPanel chartPanel = createDemoPanel();
        this.add(chartPanel, BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(550, 400));
        this.pack();
        this.setLocationRelativeTo(null);
    }

    private ChartPanel createDemoPanel() {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
                TITLE, "", "", createSampleData(),
                PlotOrientation.VERTICAL, true, true, false);

        jfreechart.setBackgroundPaint(Color.WHITE);
        jfreechart.setBorderVisible(false);
        jfreechart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 15));
        jfreechart.getTitle().setText("Mapa de Neurônios SOM");

        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setBackgroundPaint(new Color(250, 250, 250));
        xyPlot.setOutlinePaint(new Color(200, 200, 200));
        xyPlot.setOutlineStroke(new BasicStroke(0.8f));
        xyPlot.setDomainGridlinePaint(new Color(220, 220, 220));
        xyPlot.setRangeGridlinePaint(new Color(220, 220, 220));
        xyPlot.setDomainGridlineStroke(new BasicStroke(0.5f));
        xyPlot.setRangeGridlineStroke(new BasicStroke(0.5f));
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setSeriesPaint(0, new Color(52, 120, 180));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));
        renderer.setSeriesPaint(1, new Color(210, 70, 65));
        renderer.setSeriesShape(1, new Ellipse2D.Double(-5, -5, 10, 10));
        xyPlot.setRenderer(renderer);

        NumberAxis domainAxis = (NumberAxis) xyPlot.getDomainAxis();
        domainAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        domainAxis.setAxisLinePaint(new Color(180, 180, 180));

        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        rangeAxis.setAxisLinePaint(new Color(180, 180, 180));

        ChartPanel panel = new ChartPanel(jfreechart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    private XYDataset createSampleData() {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

        XYSeries series = new XYSeries("VETOR DE ENTRADAS");
        for (int i = 0; i < this.xe.length; i++) {
            series.add(this.xe[i], this.ye[i]);
        }

        XYSeries series1 = new XYSeries("NEURÔNIOS");
        for (int i = 0; i < this.xs.length; i++) {
            series1.add(this.xs[i], this.ys[i]);
        }

        xySeriesCollection.addSeries(series);
        xySeriesCollection.addSeries(series1);
        return xySeriesCollection;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            ScatterAdd demo = new ScatterAdd(TITLE, null, null, null, null);
            demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            demo.pack();
            demo.setLocationRelativeTo(null);
            demo.setVisible(true);
        });
    }
}
