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

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.security.auth.login.LoginException;

import org.eclipse.jetty.util.IO;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.Logger;
import org.photonvision.vision.pipe.MutatingPipe;

/** Represents a pipeline that passes processing to a python script. */
public class PythonPipe extends MutatingPipe<Mat, PythonPipe.PythonParams> {
    
    private static Logger logger = new Logger(PythonPipe.class, LogGroup.VisionModule);

    static final String HEXES = "0123456789ABCDEF";

    public static String getHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append("\\x").append(HEXES.charAt((b & 0xF0) >> 4))
                .append(HEXES.charAt((b & 0x0F)));
        }

        return hex.toString();
    }
    
    private static Process pythonProcess;

    /**
     * Processes this pipe.
     *
     * @param in Input for pipe processing.
     * @return The processed frame.
     */
    @Override
    protected Void process(Mat in) {
        if (pythonProcess == null) {
            try {
                pythonProcess = new ProcessBuilder()
                    .command("python", "python_pipe.py")
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
                logger.info("A python process has been started");
            } catch (IOException e) {
                logger.error("Unable to start python process", e);
            }
        }

        Mat img;
        int inType = in.type();
        
        if (in.isContinuous()) {
            img = in;
        } else {
            img = in.clone();
        }

        img.convertTo(img, CvType.CV_8UC(in.channels()));
        
        ByteBuffer shapeBuffer = ByteBuffer.allocate(12)
            .putInt(in.height())
            .putInt(in.width())
            .putInt(in.channels());
        
        byte[] in_data = new byte[(int)(in.total() * in.channels())];
        img.get(0, 0, in_data);
        
        try {
            pythonProcess.getOutputStream().write(shapeBuffer.array());
            pythonProcess.getOutputStream().write(in_data);
            pythonProcess.getOutputStream().flush();

            pythonProcess.getInputStream().read(in_data);
        } catch (IOException e) {
            logger.error("Unable to send image to python process.", e);
            pythonProcess = null;
        }

        img.put(0, 0, in_data);
        img.convertTo(in, inType);

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
