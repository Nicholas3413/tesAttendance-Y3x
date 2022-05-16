package com.example.tesattendancey3

interface SimilarityClassifier {
    class Recognition(private val id: String?, private val title: String?, private val distance: Float?) {
        var extra: Any? = null
        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }
            if (title != null) {
                resultString += "$title "
            }
            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f)
            }
            return resultString.trim { it <= ' ' }
        }
    }
}