/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seanghay.studio.gles.transition

object TransitionStore {

    fun simpleFadeTransition(): Transition = FadeTransition("fade", 1000L)

    fun getAllTransitions(): List<Transition> = listOf(
        FadeTransition("fade", 1000L),
        AngularTransition(),
        BounceTransition(),
        BowTieHorizontalTransition(),
        BowTieVerticalTransition(),
        BurnTransition(),
        ButterflyWaveScrawlerTransition(),
        CannabisleafTransition(),
        CircleCropTransition(),
        CircleTransition(),
        CircleopenTransition(),
        ColorphaseTransition(),
        ColourDistanceTransition(),
        CrazyParametricFunTransition(),
        CrossZoomTransition(),
        CrosshatchTransition(),
        CrosswarpTransition(),
        CubeTransition(),
        DirectionalTransition(),
        DirectionalwarpTransition(),
        DirectionalwipeTransition(),
        DoomScreenTransitionTransition(),
        DoorwayTransition(),
        DreamyTransition(),
        DreamyZoomTransition(),
        FadecolorTransition(),
        FadegrayscaleTransition(),
        FlyeyeTransition(),
        GlitchDisplaceTransition(),
        GlitchMemoriesTransition(),
        HeartTransition(),
        HexagonalizeTransition(),
        InvertedPageCurlTransition(),
        KaleidoscopeTransition(),
        LinearBlurTransition(),
        LuminanceMeltTransition(),
        MorphTransition(),
        MosaicTransition(),
        MultiplyBlendTransition(),
        PerlinTransition(),
        PinwheelTransition(),
        PolarFunctionTransition(),
        PolkaDotsCurtainTransition(),
        RadialTransition(),
        RippleTransition(),
        RotateScaleFadeTransition(),
        SimpleZoomTransition(),
        SqueezeTransition(),
        StereoViewerTransition(),
        SwapTransition(),
        SwirlTransition(),
        UndulatingBurnOutTransition(),
        WaterDropTransition(),
        WindTransition(),
        WindowblindsTransition(),
        WindowsliceTransition(),
        WipeDownTransition(),
        WipeLeftTransition(),
        WipeRightTransition(),
        WipeUpTransition(),
        ZoomInCirclesTransition()
    )
}
