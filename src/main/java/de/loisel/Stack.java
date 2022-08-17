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

public class Stack {
    private static final byte STACK_SIZE = 64;
    private final short[] stack = new short[STACK_SIZE];
    private byte stackPointer = 0;

    public byte getStackPointer() {
        return stackPointer;
    }

    public void push(short addr) {
        stackPointer++;
        if (stackPointer >= STACK_SIZE) // stack overflow
            stackPointer = 0;
        stack[stackPointer] = addr;
    }

    public short pop() {
        short addr = stack[stackPointer];
        stackPointer--;
        if (stackPointer < 0) // stack overflow
            stackPointer = STACK_SIZE - 1;
        return addr;
    }
}
