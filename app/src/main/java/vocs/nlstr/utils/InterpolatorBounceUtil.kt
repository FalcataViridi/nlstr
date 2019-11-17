package vocs.nlstr.utils

internal class InterpolatorBounceUtil(type: AnimationEffectTypes) :android.view.animation.Interpolator {
    private var mAmplitude = 1.0
    private var mFrequency = 10.0

    init {
        when (type) {
            AnimationEffectTypes.BUBBLE -> {
                mAmplitude = 0.2
                mFrequency = 25.0
            }
        }
    }

    override fun getInterpolation(time: Float): Float {
        return (-1.0 * Math.pow(Math.E, -time / mAmplitude) *
                Math.cos(mFrequency * time) + 1).toFloat()
    }
}