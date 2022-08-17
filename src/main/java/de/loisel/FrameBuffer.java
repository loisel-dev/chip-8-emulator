/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.loisel;

import java.util.Arrays;

public class FrameBuffer {
    private boolean[][] buffer;

    public FrameBuffer() {
        buffer = new boolean[64][32];
    }

    public synchronized boolean setPixel(int x, int y) {
        buffer[x][y] = !buffer[x][y];
        return !buffer[x][y];
    }

    public synchronized void clearBuffer() {
        buffer = new boolean[64][32];
    }

    public synchronized boolean[][] copyBuffer() {
        return Arrays.stream(buffer).map(boolean[]::clone).toArray(boolean[][]::new);
    }

    /**
     * returns true on pixel collision
     */
    public synchronized boolean setSprite(byte[] sprite, byte xCord, byte yCord) {

        int xC = Byte.toUnsignedInt(xCord) % 64;
        int yC = Byte.toUnsignedInt(yCord) % 32;

        boolean collision = false;
        for (int i = 0; (i < sprite.length) && (yC + i < 32); i++) {
            String byteString = String.format("%8s", Integer.toBinaryString(sprite[i] & 0xFF)).replace(' ', '0');
            for (int j = 0; (j < 8) && (xC + j < 64); j++) {
                if(byteString.charAt(j) == '1') {
                    collision = buffer[xC + j][yC + i];
                    buffer[xC + j][yC + i] = !buffer[xC + j][yC + i];
                }
            }
        }
        return collision;
    }
}
