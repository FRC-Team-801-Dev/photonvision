/*
 * Copyright (C) Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.photonvision.vision.pipe.impl;

import org.opencv.core.Mat;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.Logger;
import org.photonvision.vision.pipe.MutatingPipe;

/** Represents a pipeline that passes processing to a python script. */
public class PythonPipe extends MutatingPipe<Mat, PythonPipe.PythonParams> {
    
    private static Logger logger = new Logger(PythonPipe.class, LogGroup.VisionModule);
    /**
     * Processes this pipe.
     *
     * @param in Input for pipe processing.
     * @return The processed frame.
     */
    @Override
    protected Void process(Mat in) {
        logger.debug("Pretend a python process in running");
        return null;
    }

    public static class PythonParams {
        // Default PythonImagePrams with a no-op module.
        public static PythonParams DEFAULT = new PythonParams("moduleName");
        

        // Member to store the name of the python module to run.
        private final String m_moduleName;

        /**
         * Constructs a new PythonImageParams.
         *
         * @param moduleName The name of the python module to run.
         */
        public PythonParams(String moduleName) {
            m_moduleName = moduleName;
        }

        /**
         * Returns the name of python module to run.
         *
         * @return The name of the python module to run.
         */
        public String getPythonModuleName() {
            return m_moduleName;
        }
    }
}
