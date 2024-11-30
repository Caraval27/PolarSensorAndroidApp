package com.example.bluetoothapp.presentation.components

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

fun setupLineChart(
    lineChart: LineChart,
    linearValues: List<Float>,
    fusionValues: List<Float>,
    visibleRange: Int = 50
) {
    val linearEntries = linearValues.mapIndexed { index, value ->
        Entry(index.toFloat(), value)
    }

    val fusionEntries = fusionValues.mapIndexed { index, value ->
        Entry(index.toFloat(), value)
    }

    val linearDataSet = LineDataSet(linearEntries, "Linear sensor data").apply {
        color = Color.BLUE
        valueTextColor = Color.BLUE
        lineWidth = 2f
        setCircleColor(Color.BLUE)
        circleRadius = 3f
        setDrawValues(true) // så jag kan se värden, false sen
    }

    val fusionDataSet = LineDataSet(fusionEntries, "Fusion sensor data").apply {
        color = Color.RED
        valueTextColor = Color.RED
        lineWidth = 2f
        setCircleColor(Color.RED)
        circleRadius = 3f
        setDrawValues(true) // så jag kan se värden, false sen
    }

    lineChart.apply {
        data = LineData(linearDataSet, fusionDataSet)
        description.isEnabled = false
        setTouchEnabled(true)
        setPinchZoom(true)
        setBackgroundColor(Color.BLACK) // ändrar färger sen
        setNoDataText("No data available")
        setNoDataTextColor(Color.RED)

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        axisLeft.apply {
            textColor = Color.WHITE
            setDrawGridLines(true)
        }

        axisRight.isEnabled = false

        legend.apply {
            isEnabled = true
            textColor = Color.WHITE
            textSize = 12f
            form = Legend.LegendForm.LINE
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }

        setVisibleXRangeMaximum(visibleRange.toFloat())

        if (fusionEntries.isNotEmpty()) {
            moveViewToX(fusionEntries.last().x - visibleRange)
        }

        invalidate()
    }
}