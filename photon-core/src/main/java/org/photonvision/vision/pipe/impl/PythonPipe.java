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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Mat;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.Logger;
import org.photonvision.vision.pipe.MutatingPipe;

public class PythonPipe extends MutatingPipe<Mat, PythonPipe.PythonParams> {

    private static final Logger logger = new Logger(SolvePNPPipe.class, LogGroup.VisionModule);
    
    private OutputStream pythonStdIn;
    private InputStream pythonStdOut;
    private Process pythonProcess;

    public PythonPipe() {
        super();
        try {
            ArrayList<String> command = new ArrayList<String>();
            command.add("python");
            command.add("python_pipe.py");
            command.add(params.moduleName);
            params.kwargs.forEach((k, v) -> {
                command.add(k + "=" + v);
            });


            logger.debug("Starting python process.");
            pythonProcess = new ProcessBuilder().command(command).inheritIO().start();
            pythonStdIn = pythonProcess.getOutputStream();
            pythonStdOut = pythonProcess.getInputStream();
        } catch (IOException e) {
            logger.error("Unable to start python listener", e);
        }
    }
    
    @Override
    protected Void process(Mat in) {
        byte[] buffer = new byte[(int) (in.total() * in.channels())];
        in.get(0, 0, buffer);

        try {
            pythonStdIn.write(ByteBuffer.allocate(12)
                .putInt(in.width())
                .putInt(in.height())
                .putInt(in.channels())
            .array());
            pythonStdIn.write(buffer);

            pythonStdOut.read(buffer);
            in.put(0, 0, buffer);
        } catch (IOException e) {
            logger.error("Unable to process python Pipe", e);
        }
        return null;
    }

    public static class PythonParams {
        
        String moduleName;
        Map<String, String> kwargs;

        public PythonParams(String moduleName) {
            this(moduleName, new HashMap<String, String>());
        }

        public PythonParams(String moduleName, Map<String, String> kwargs) {
            this.moduleName = moduleName;
            this.kwargs = kwargs;
        }

        public String GetName() {
            return moduleName;
        }

        public Map<String, String> getMap() {
            return kwargs;
        }
    }
}