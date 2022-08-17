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

public class Keyboard {
    private boolean[] keys;

    public Keyboard() {
        keys = new boolean[0x10];
    }

    public synchronized boolean isDown(byte key) {
        key &= 0xF;
        return keys[key];
    }

    public synchronized void set(byte key) {
        key &= 0xF;
        keys[key] = true;
    }

    public synchronized void unset(byte key) {
        key &= 0xF;
        keys[key] = false;
    }

    public synchronized void toggleKeyState(byte key) {
        key &= 0xF;
        keys[key] = !keys[key];
    }

    public synchronized void resetKeys() {
        keys = new boolean[0xf];
    }

    public synchronized byte getNexKey() {
        for (byte i = 0; i < keys.length; i++) {
            if (keys[i])
                return i;
        }
        return (byte)0xFF;
    }
}
