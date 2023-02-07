package com.dan.timelapse

class OutputParams {
    companion object {
        const val VALUE_UNKNOWN = Int.MIN_VALUE

        const val COMPARE_NOT_CHANGED = 0
        const val COMPARE_CHANGED_ONLY_FPS = 1
        const val COMPARE_CHANGED = 2

        const val KEY_FPS = "FPS"
        const val KEY_SPEED = "SPEED"
        const val KEY_ALIGN = "ALIGN"
        const val KEY_EFFECT = "EFFECT"
        const val KEY_EFFECT_SIZE = "EFFECT-SIZE"

        private fun compare(a: Map<String, Int>, b: Map<String, Int>): Int {
            var fpsChanged = false

            a.forEach { (key, value) ->
                if (value != b.getOrDefault(key, VALUE_UNKNOWN)) {
                    if (KEY_FPS == key) {
                        fpsChanged = true
                    } else {
                        return COMPARE_CHANGED
                    }
                }
            }

            return if (fpsChanged) COMPARE_CHANGED_ONLY_FPS else COMPARE_NOT_CHANGED
        }
    }

    private val _params = mutableMapOf<String, Int>()

    fun set(key: String, value: Int) {
        _params[key] = value
    }

    fun get(key: String): Int {
        return _params.getOrDefault(key, VALUE_UNKNOWN)
    }

    fun compareWith(other: OutputParams?): Int {
        if (null == other) return COMPARE_CHANGED

        val compare1 = compare(_params, other._params)
        if (COMPARE_CHANGED == compare1) return COMPARE_CHANGED

        val compare2 = compare(other._params, _params)
        if (COMPARE_CHANGED == compare2) return COMPARE_CHANGED

        if (COMPARE_CHANGED_ONLY_FPS == compare1 || COMPARE_CHANGED_ONLY_FPS == compare2) return COMPARE_CHANGED_ONLY_FPS
        return COMPARE_NOT_CHANGED
    }
}