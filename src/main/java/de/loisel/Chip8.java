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

import java.util.Random;

public class Chip8 implements Runnable{
    Random rand;
    private boolean isRunning;
    private long clockSpeed;
    private int emptyInst;

    private final byte[] vReg;
    private short programCounter;
    private short indexReg;
    private int delayTimer;
    private int soundTimer;

    private final Memory memory;
    private final Stack stack;
    private final FrameBuffer frameBuffer;
    private final Keyboard keyboard;

    public Chip8(Program program, FrameBuffer frameBuffer, Keyboard keyboard) {
        this.frameBuffer = frameBuffer;
        this.keyboard = keyboard;
        this.memory = new Memory();
        this.stack = new Stack();

        this.programCounter = 0x200;
        this.indexReg = 0;
        this.delayTimer = 0;
        this.soundTimer = 0;
        this.vReg = new byte[16];

        this.isRunning = false;
        this.clockSpeed = 500;
        this.emptyInst = 0;
        this.rand = new Random();

        byte[] rawProgram = program.getProgram();
        for (int i = programCounter; i < (programCounter + rawProgram.length); i++) {
            memory.write((short)i, rawProgram[i - programCounter]);
        }
    }

    public synchronized boolean isSound() {
        return soundTimer > 0;
    }

    @Override
    public void run() {
        runProgram();
    }

    public void debugRegs() {
        System.out.println("Registers:\n");
        for(int i = 0; i <= 9; i++) {
            System.out.println("V" + i + ": " + vReg[i]);
        }
        System.out.println("VA: " + vReg[10]);
        System.out.println("VB: " + vReg[11]);
        System.out.println("VC: " + vReg[12]);
        System.out.println("VD: " + vReg[13]);
        System.out.println("VE: " + vReg[14]);
        System.out.println("VF: " + vReg[15]);
        System.out.println("PC: " + programCounter);
        System.out.println("SP: " + stack.getStackPointer());
        System.out.println("I:  " + indexReg);
        System.out.println("DT: " + delayTimer);
        System.out.println("ST: " + soundTimer);
    }

    public void setClockSpeed(long clockSpeed) {
        this.clockSpeed = clockSpeed;
    }

    private void runProgram() {
        this.loop();
    }

    private void loop() {
        long lastCycle = System.nanoTime();
        long lastTimerUpdate = System.nanoTime();
        isRunning = true;

        /* debug info */
        long cycleCount = 0;
        long startTime = System.currentTimeMillis();
        /* debug info */

        while(isRunning) {
            long currentTime = System.nanoTime();

            if(currentTime - lastCycle >= (1e9F/clockSpeed)) {
                cycle();
                lastCycle = currentTime;
                cycleCount++;
            }

            if(currentTime - lastTimerUpdate >= (1e9F/60L)) {
                if(delayTimer > 0)
                    delayTimer--;
                if(soundTimer > 0)
                    soundTimer --;
                lastTimerUpdate = currentTime;
            }
        }

        System.out.println("Program ran " + (System.currentTimeMillis() - startTime) + " milliseconds");
        System.out.println("Executed " + cycleCount + " cycles");
    }

    private void cycle() {
        // Fetch
        byte b1 = memory.fetch(programCounter);
        incrementPC();
        byte b2 = memory.fetch(programCounter);
        incrementPC();
        short instruction = (short) ((b1 << 8) | (b2 & 0xFF));
        String instString = Integer.toHexString(instruction & 0xffff).toUpperCase();
        if (instString.length() == 1) instString = "000" + instString;
        if (instString.length() == 2) instString = "00" + instString;
        if (instString.length() == 3) instString = "0" + instString;

        // Decode and Execute
        final byte regX = hexCharToByte('0', instString.charAt(1));
        final byte regY = hexCharToByte('0', instString.charAt(2));
        final byte kk = hexCharToByte(instString.charAt(2), instString.charAt(3));
        final short nnn = (short) setNibble(instruction, 0, 3);

        switch(instString.toUpperCase().charAt(0)) {
            case '0':
                switch (instString.substring(1)) {
                    case "000" -> emptyInstCounter(programCounter);                                       // 0000 - Empty
                    case "0E0" -> frameBuffer.clearBuffer();                                // 00E0 - CLS
                    case "0EE" -> programCounter = stack.pop();                             // 00EE - RET
                    default ->
                            System.out.println(
                                    "Instruction under 0xxx not found: " + instString);
                }
                break;
            case '1':                                                                       // 1nnn - JP addr
                programCounter = nnn;
                break;
            case '2':                                                                       // 2nnn - CALL addr
                stack.push(programCounter);
                programCounter = nnn;
                break;
            case '3':                                                                       // 3xkk - SE Vx, byte
                if (vReg[regX] == kk) {incrementPC(); incrementPC();}
                break;
            case '4':                                                                       // 4xkk - SNE Vx, byte
                if (vReg[regX] != kk) {incrementPC(); incrementPC();}
                break;
            case '5':                                                                       // 5xy0 - SE Vx, Vy
                if (vReg[regX] == vReg[regY]) {incrementPC(); incrementPC();}
                break;
            case '6':                                                                       // 6xkk - LD Vx, byte
                vReg[regX] = kk;
                break;
            case '7':                                                                       // 7xkk - ADD Vx, byte
                vReg[regX] += kk;
                break;
            case '8':
                switch (instString.toUpperCase().charAt(3)) {
                    case '0':                                                               // 8xy0 - LD Vx, Vy
                        vReg[regX] = vReg[regY];
                        break;
                    case '1':                                                               // 8xy1 - OR Vx, Vy
                        vReg[regX] |= vReg[regY];
                        break;
                    case '2':                                                               // 8xy2 - AND Vx, Vy
                        vReg[regX] &= vReg[regY];
                        break;
                    case '3':                                                               // 8xy3 - XOR Vx, Vy
                        vReg[regX] ^= vReg[regY];
                        break;
                    case '4':                                                               // 8xy4 - ADD Vx, Vy
                        if (Byte.toUnsignedInt(vReg[regX]) + Byte.toUnsignedInt(vReg[regY]) > 255)
                            vReg[15] = 1;
                        else
                            vReg[15] = 0;
                        vReg[regX] = (byte) (Byte.toUnsignedInt(vReg[regX]) + Byte.toUnsignedInt(vReg[regY]));
                        break;
                    case '5':                                                               // 8xy5 - SUB Vx, Vy
                        if (Byte.toUnsignedInt(vReg[regX]) < Byte.toUnsignedInt(vReg[regY]))
                            vReg[15] = 0;
                        else
                            vReg[15] = 1;
                        vReg[regX] = (byte)(vReg[regX] - vReg[regY]);
                        break;
                    case '6':                                                               // 8xy6 - SHR Vx {, Vy}
                        vReg[15] = ((vReg[regX] & 1) == 0) ? 0 : (byte)1;
                        vReg[regX] >>= 1;
                        break;
                    case '7':                                                               // 8xy7 - SUBN Vx, Vy
                        if (Byte.toUnsignedInt(vReg[regY]) < Byte.toUnsignedInt(vReg[regX]))
                            vReg[15] = 0;
                        else
                            vReg[15] = 1;
                        vReg[regX] = (byte)(vReg[regY] - vReg[regX]);
                        break;
                    case 'E':                                                               // 8xyE - SHL Vx {, Vy}
                        vReg[15] = (Byte.toUnsignedInt(vReg[regX]) < 128) ? 0 : (byte)1;
                        vReg[regX] <<= 1;
                        break;
                    default:
                        System.out.println(
                                "Instruction under 8xyx not found: " + instString);
                        break;
                }
                break;
            case '9':                                                                       // 9xy0 - SNE Vx, Vy
                if (vReg[regX] != vReg[regY]) { incrementPC(); incrementPC(); }
                break;
            case 'A':                                                                       // Annn - LD I, addr
                indexReg = nnn;
                break;
            case 'B':                                                                       // Bnnn - JP V0, addr
                programCounter = (short)(nnn + vReg[0]);
                break;
            case 'C':                                                                       // Cxkk - RND Vx, byte
                vReg[regX] = (byte)(kk & (byte)rand.nextInt(0, 256));
                break;
            case 'D':                                                                       // Dxyn - DRW Vx, Vy, nibble
                byte[] sprite = new byte[hexCharToByte('0', instString.charAt(3))];
                for (int i = 0; i < sprite.length; i++) {
                    sprite[i] = memory.fetch((short)(indexReg + i));
                }
                boolean collision = frameBuffer.setSprite(sprite, vReg[regX], vReg[regY]);
                if(collision) vReg[15] = 1;
                else vReg[15] = 0;
                break;
            case 'E':
                switch (instString.toUpperCase().charAt(3)) {
                    case 'E':                                                               // Ex9E - SKP Vx
                        if(keyboard.isDown(vReg[regX])) { incrementPC(); incrementPC(); }
                        break;
                    case '1':                                                               // ExA1 - SKNP Vx
                        if(!keyboard.isDown(vReg[regX])) { incrementPC(); incrementPC(); }
                        break;
                    default:
                        System.out.println(
                                "Instruction under ExNN not found: " + instString);
                        break;
                }
                break;
            case 'F':
                switch (kk) {
                    case 0x07:                                                              // Fx07 - LD Vx, DT
                        vReg[regX] = (byte) delayTimer;
                        break;
                    case 0x0A:                                                              // Fx0A - LD Vx, K
                        do { vReg[regX] = keyboard.getNexKey(); } while(vReg[regX] == (byte) 0xFF);
                        break;
                    case 0x15:                                                              // Fx15 - LD DT, Vx
                        delayTimer = Byte.toUnsignedInt(vReg[regX]);
                        break;
                    case 0x18:                                                              // Fx18 - LD ST, Vx
                        soundTimer = Byte.toUnsignedInt(vReg[regX]);
                        break;
                    case 0x1E:                                                              // Fx1E - ADD I, Vx
                        indexReg = (short) (Short.toUnsignedInt(indexReg) + Byte.toUnsignedInt(vReg[regX]));
                        break;
                    case 0x29:                                                              // Fx29 - LD F, Vx
                        indexReg = (short)(memory.font() + vReg[regX] * 5);
                        break;
                    case 0x33:                                                              // Fx33 - LD B, Vx
                        int byt = Byte.toUnsignedInt(vReg[regX]);
                        memory.write(indexReg, (byte) (byt / 100));
                        memory.write((short) (indexReg + 1), (byte) ((byt % 100) / 10));
                        memory.write((short) (indexReg + 2), (byte) ((byt % 100) % 10));
                        break;
                    case 0x55:                                                              // Fx55 - LD [I], Vx
                        // modern interpreter don't increment the index register here
                        // if running old roms this could cause some trouble
                        for (byte i = 0; i <= regX; i++)
                            memory.write((short) (indexReg + i), vReg[i]);
                        break;
                    case 0x65:                                                              // Fx65 - LD Vx, [I]
                        // same problem with index register as with Fx55
                        for (byte i = 0; i <= regX; i++)
                            vReg[i] = memory.fetch((short) (indexReg + i));
                        break;
                    default:
                        System.out.println(
                                "Instruction under FxNN not found: " + instString);
                        break;
                }
                break;
            default:
                System.out.println("Instruction not found: " + instString);
        }
    }

    private void emptyInstCounter(short position) {
        System.out.println("Empty instruction at " + position);
        emptyInst++;
        if (emptyInst >= 5) {
            isRunning = false;
            System.out.println("Abort process because of multiple empty instructions");
        }
    }

    private void incrementPC() {
        this.programCounter++;
        if (programCounter >= Memory.MEMORY_SIZE)
            programCounter = 0;
    }

    private static boolean willAdditionOverflow(byte left, byte right) {
        if (right < 0 && right != Byte.MIN_VALUE) {
            return willSubtractionOverflow(left, (byte)-right);
        } else {
            return (~(left ^ right) & (left ^ (left + right))) < 0;
        }
    }

    private static boolean willSubtractionOverflow(byte left, byte right) {
        if (right < 0) {
            return willAdditionOverflow(left, (byte)-right);
        } else {
            return ((left ^ right) & (left ^ (left - right))) < 0;
        }
    }

    private static int setNibble(int num) {
        return setNibble(num, 0xF, 0);
    }

    private static int setNibble(int num, int nibble, int which) {
        int shiftNibble= nibble << (4*which) ;
        int shiftMask= 0x0000000F << (4*which) ;
        return ( num & ~shiftMask ) | shiftNibble ;
    }

    private static byte hexCharToByte(char c1, char c2) {
        return (byte) ((toDigit(c1) << 4) + toDigit(c2));
    }

    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }

}
