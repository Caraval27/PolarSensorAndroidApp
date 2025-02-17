package com.example.bluetoothapp.presentation.components

import android.graphics.Color
import com.example.bluetoothapp.domain.Sample
import com.example.bluetoothapp.presentation.viewModel.RecordingState
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

fun setupLineChart(
    lineChart: LineChart,
    linearValues: List<Sample>,
    fusionValues: List<Sample>,
    recordingState: RecordingState,
    visibleRange: Int = 50
) {
    val linearEntries = linearValues.map { sample ->
        Entry(sample.sequenceNumber.toFloat(), sample.value)
    }

    val fusionEntries = fusionValues.map { sample ->
        Entry(sample.sequenceNumber.toFloat(), sample.value)
    }

    val linearDataSet = LineDataSet(linearEntries, "Accelerometer").apply {
        color = Color.rgb(255, 152, 0)
        lineWidth = 2f
        setDrawCircles(false)
        setDrawValues(false)
    }

    val fusionDataSet = LineDataSet(fusionEntries, "Fusion with gyroscope").apply {
        color = Color.rgb(118, 199, 192)
        lineWidth = 2f
        setDrawCircles(false)
        setDrawValues(false)
    }

    lineChart.apply {
        data = LineData(linearDataSet, fusionDataSet)
        description.isEnabled = false
        setTouchEnabled(true)
        if (recordingState == RecordingState.Ongoing) {
            setTouchEnabled(false)
        } else {
            setTouchEnabled(true)
        }
        setBackgroundColor(Color.rgb(46, 52, 64))
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
            axisMinimum = -10f
            axisMaximum = 100f
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

        if (recordingState != RecordingState.Ongoing) {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        lineChart.centerViewToAnimated(
                            e.x,
                            e.y,
                            lineChart.data.getDataSetForEntry(e).axisDependency,
                            500
                        )
                    }
                }
                override fun onNothingSelected() {}
            })
        }

        if (fusionEntries.isNotEmpty()) {
            moveViewToX(fusionEntries.last().x - visibleRange)
        }

        invalidate()
    }
}