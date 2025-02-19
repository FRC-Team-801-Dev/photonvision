<template>
  <div>
    <CVslider
      v-model="cameraExposure"
      name="Exposure"
      min="0"
      max="100"
      step="0.1"
      tooltip="Directly controls how much light is allowed to fall onto the sensor, which affects brightness"
      :slider-cols="largeBox"
      @input="handlePipelineData('cameraExposure')"
      @rollback="e => rollback('cameraExposure', e)"
    />
    <CVslider
      v-model="cameraBrightness"
      name="Brightness"
      min="0"
      max="100"
      tooltip="Controls camera postprocessing that brightens or darkens the image uniformly"
      :slider-cols="largeBox"
      @input="handlePipelineData('cameraBrightness')"
      @rollback="e => rollback('cameraBrightness', e)"
    />
    <CVslider
      v-if="cameraGain !== -1"
      v-model="cameraGain"
      name="Gain"
      min="0"
      max="100"
      tooltip="Controls automatic white balance gain, which affects how the camera captures colors in different conditions"
      :slider-cols="largeBox"
      @input="handlePipelineData('cameraGain')"
      @rollback="e => rollback('cameraGain', e)"
    />
    <CVselect
      v-model="inputImageRotationMode"
      name="Orientation"
      tooltip="Rotates the camera stream"
      :list="['Normal','90° CW','180°','90° CCW']"
      :select-cols="largeBox"
      @input="handlePipelineData('inputImageRotationMode')"
      @rollback="e => rollback('inputImageRotationMode',e)"
    />
    <CVselect
      v-model="cameraVideoModeIndex"
      name="Resolution"
      tooltip="Resolution and FPS the camera should directly capture at"
      :list="resolutionList"
      :select-cols="largeBox"
      @input="handlePipelineData('cameraVideoModeIndex')"
      @rollback="e => rollback('cameraVideoModeIndex', e)"
    />
    <CVselect
      v-model="streamingFrameDivisor"
      name="Stream Resolution"
      tooltip="Resolution to which camera frames are downscaled for streaming to the dashboard"
      :list="streamResolutionList"
      :select-cols="largeBox"
      @rollback="e => rollback('streamingFrameDivisor', e)"
    />
  </div>
</template>

<script>
    import CVslider from '../../components/common/cv-slider'
    import CVselect from '../../components/common/cv-select'

    const unfilteredStreamDivisors = [1, 2, 4, 6];

    export default {
        name: 'Input',
        components: {
            CVslider,
            CVselect,
        },
        // eslint-disable-next-line vue/require-prop-types
        props: ['value'],
        data() {
            return {
              rawStreamDivisorIndex: 0,
            }
        },
        computed: {
            largeBox: {
              get() {
                // Sliders and selectors should be fuller width if we're on screen size medium and
                // up and either not in compact mode (because the tab will be 100% screen width),
                // or in driver mode (where the card will also be 100% screen width).
                return this.$vuetify.breakpoint.mdAndUp && (!this.$store.state.compactMode || this.$store.getters.isDriverMode) ? 10 : 8;
              }
            },
            cameraExposure: {
                get() {
                    return parseFloat(this.$store.getters.currentPipelineSettings.cameraExposure);
                },
                set(val) {
                    this.$store.commit("mutatePipeline", {"cameraExposure": parseFloat(val)});
                }
            },
            cameraBrightness: {
                get() {
                    return parseInt(this.$store.getters.currentPipelineSettings.cameraBrightness)
                },
                set(val) {
                    this.$store.commit("mutatePipeline", {"cameraBrightness": parseInt(val)});
                }
            },
            cameraGain: {
                get() {
                    return parseInt(this.$store.getters.currentPipelineSettings.cameraGain)
                },
                set(val) {
                    this.$store.commit("mutatePipeline", {"cameraGain": parseInt(val)});
                }
            },
            inputImageRotationMode: {
                get() {
                    return this.$store.getters.currentPipelineSettings.inputImageRotationMode
                },
                set(val) {
                    this.$store.commit("mutatePipeline", {"inputImageRotationMode": val});
                }
            },
            cameraVideoModeIndex: {
                get() {
                    return this.$store.getters.currentPipelineSettings.cameraVideoModeIndex;
                },
                set(val) {
                    this.$store.commit("mutatePipeline", {"cameraVideoModeIndex": val});

                    this.handlePipelineUpdate("streamingFrameDivisor", this.getNumSkippedStreamDivisors());
                    this.rawStreamDivisorIndex = 0;
                }
            },
            streamingFrameDivisor: {
                get() {
                    return this.rawStreamDivisorIndex;
                },
                set(val) {
                    this.rawStreamDivisorIndex = val;
                    this.handlePipelineUpdate("streamingFrameDivisor", this.getNumSkippedStreamDivisors() + val);
                }
            },

            resolutionList: {
                get() {
                    let tmp_list = [];
                    for (let i of this.$store.getters.videoFormatList) {
                        tmp_list.push(`${i['width']} X ${i['height']} at ${i['fps']} FPS, ${i['pixelFormat']}`)
                    }
                    return tmp_list;
                }
            },

            streamResolutionList: {
                get() {
                    const cam_res = this.$store.getters.videoFormatList[
                        this.$store.getters.currentCameraSettings.currentPipelineSettings.cameraVideoModeIndex];
                    let tmp_list = [];
                    for (const x of this.getRawStreamDivisors()) {
                        tmp_list.push(`${Math.floor(cam_res['width'] / x)} X ${Math.floor(cam_res['height'] / x)}`);
                    }
                    return tmp_list;
                }
            }
        },
        methods: {
          getRawStreamDivisors() {
            // Limit stream res when GPU acceleration is enabled because we *know* that we won't be able to get smooth streams above ~640x480
            // It would probably be cleaner if this checked that we're on the Raspi 3 instead of checking for GPU accel status
            const width = this.$store.getters.videoFormatList[
                    this.$store.getters.currentCameraSettings.currentPipelineSettings.cameraVideoModeIndex]['width'];

            // If GPU acceleration is enabled, the  downsized width must be below 400px
            // This check should be skipped if we're currently in driver mode
            return unfilteredStreamDivisors.filter((x) => this.$store.getters.isDriverMode
                    || !this.$store.state.settings.general.gpuAcceleration || width / x < 400);
          },
          getNumSkippedStreamDivisors() {
            return unfilteredStreamDivisors.length - this.getRawStreamDivisors().length;
          }
        }
    }
</script>

<style scoped>

</style>
