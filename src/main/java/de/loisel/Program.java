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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Program {
    private final List<Byte> rawProgram;

    public Program(String romFile) {
        rawProgram = new ArrayList<>();

        try (InputStream inputStream = new FileInputStream(romFile)) {

            byte[] data = inputStream.readAllBytes();
            for(byte byt: data) {
                rawProgram.add(byt);
            }

        } catch(IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }

    public byte[] getProgram() {
        byte[] program = new byte[rawProgram.size()];
        for (int i = 0; i < rawProgram.size(); i++) {
            program[i] = rawProgram.get(i);
        }
        return program;
    }
}
