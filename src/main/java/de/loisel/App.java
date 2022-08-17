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

import processing.core.PApplet;

public class App extends PApplet {
    private static FrameBuffer frameBuffer;
    private static Keyboard keyboard;

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 960;
    private static final int FADE_LEN = 7;

    long lastCount = 0;
    int fCounter = 0;

    boolean[][] oldImage;
    int [][] pixelFade;

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(60);
        noStroke();
        background(0);
    }

    @Override
    public void keyPressed() {
        setKey(key, true);
    }

    @Override
    public void keyReleased() {
        setKey(key, false);
    }

    private static void setKey(char k, boolean state) {
        byte key;
        switch (k) {
            case '1' -> key = 0x1;
            case '2' -> key = 0x2;
            case '3' -> key = 0x3;
            case '4' -> key = 0xC;
            case 'q' -> key = 0x4;
            case 'w' -> key = 0x5;
            case 'e' -> key = 0x6;
            case 'r' -> key = 0xD;
            case 'a' -> key = 0x7;
            case 's' -> key = 0x8;
            case 'd' -> key = 0x9;
            case 'f' -> key = 0xE;
            case 'y' -> key = 0xA;
            case 'x' -> key = 0x0;
            case 'c' -> key = 0xB;
            case 'v' -> key = 0xF;
            default -> key = (byte) 16;
        }
        if (key < 16)
            if (state)
                keyboard.set(key);
            else
                keyboard.unset(key);
    }

    @Override
    public void draw() {
        boolean[][] image = frameBuffer.copyBuffer();
        if(pixelFade == null)
            pixelFade = new int[image.length][image[0].length];

        if(oldImage != null) {
            for(int x = 0; x < image.length; x++) {
                for(int y = 0; y < image[0].length; y++) {
                    if((image[x][y] != oldImage[x][y])) {
                        if(image[x][y]) {
                            fill(50, 100, 0);
                            pixelFade[x][y] = 0;
                        }
                        else {
                            //fill(0);
                            pixelFade[x][y] = FADE_LEN;
                        }
                        fillQuad(x, y);
                    } else if (pixelFade[x][y] > 0) {
                        if (pixelFade[x][y] > 1)
                            fill(50, 100, 0);
                        else
                            fill(0,0,0);
                        fillQuad(x, y);
                        pixelFade[x][y]--;
                    }
                }
            }
        }
        oldImage = image;

        countFPS();
    }

    private void fillQuad(int x, int y) {
        quad(
                x * WIDTH / 64F, y * HEIGHT / 32F,
                x * WIDTH / 64F + WIDTH / 64F,  y * HEIGHT / 32F,
                x * WIDTH / 64F + WIDTH / 64F,  y * HEIGHT / 32F + HEIGHT / 32F,
                x * WIDTH / 64F, y * HEIGHT / 32F + HEIGHT / 32F
        );
    }

    private void countFPS() {
        fCounter++;
        if(System.currentTimeMillis() > lastCount + 1000) {
            System.out.println("FPS: " + fCounter);
            fCounter = 0;
            lastCount = System.currentTimeMillis();
        }
    }

    public static void main( String[] args ) {

        if (args == null || args.length == 0) {
            throw new RuntimeException("No arguments found. Cannot load program!");
        }

        frameBuffer = new FrameBuffer();
        Program program = new Program(args[0]);
        keyboard = new Keyboard();

        Chip8 chip = new Chip8(program, frameBuffer, keyboard);

        ChipAudio audio = new ChipAudio(chip);

        String[] appletArgs = new String[] { "Chip8 by loisel" };
        PApplet.runSketch(appletArgs, new App());

        sleep(1000); // wait a little for processing

        Thread chipThread = new Thread(chip, "Chip Thread");
        Thread audioThread = new Thread(audio, "Audio Thread");
        chipThread.start();
        audioThread.start();

    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
