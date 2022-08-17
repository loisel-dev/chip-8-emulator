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

public class Memory {
    public static final int MEMORY_SIZE = 4096;

    private byte[] memory;
    private final int fontOffset;

    public Memory() {
        this(0x050);
    }

    public Memory(int fontOffset) {
        this.memory = new byte[MEMORY_SIZE];
        this.fontOffset = fontOffset;
        this.writeFonts();
        System.out.println("Memory initialized!");
    }

    public byte fetch(short address) {
        int addr = Short.toUnsignedInt(address);
        addr %= MEMORY_SIZE;
        return memory[addr];
    }

    public void write(short address, byte data) {
        int addr = Short.toUnsignedInt(address);
        addr %= MEMORY_SIZE;
        memory[addr] = data;
    }

    private void writeFonts() {
        int[] font = {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80, // F
        };

        for(int i = 0; i < font.length; i++) {
            memory[fontOffset + i] = (byte) font[i];
        }
    }

    public void reset() {
        this.memory = new byte[MEMORY_SIZE];
        this.writeFonts();
    }

    public short font() {
        return (short) fontOffset;
    }
}
