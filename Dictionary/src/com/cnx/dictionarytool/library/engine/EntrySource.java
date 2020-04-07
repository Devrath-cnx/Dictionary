// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cnx.dictionarytool.library.engine;

import com.cnx.dictionarytool.library.collections.IndexedObject;
import com.cnx.dictionarytool.library.raf.RAFListSerializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EntrySource extends IndexedObject {

    final String name;
    @SuppressWarnings("CanBeFinal")
    int numEntries;

    @SuppressWarnings("WeakerAccess")
    public EntrySource(final int index, final String name, int numEntries) {
        super(index);
        this.name = name;
        this.numEntries = numEntries;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getNumEntries() {
        return numEntries;
    }

    public String getName() {
        return name;
    }

    public static final class Serializer implements RAFListSerializer<EntrySource> {

        final Dictionary dictionary;

        Serializer(Dictionary dictionary) {
            this.dictionary = dictionary;
        }

        @Override
        public EntrySource read(DataInput raf, int readIndex)
        throws IOException {
            final String name = raf.readUTF();
            final int numEntries = dictionary.dictFileVersion >= 3 ? raf.readInt() : 0;
            return new EntrySource(readIndex, name, numEntries);
        }

        @Override
        public void write(DataOutput raf, EntrySource t) throws IOException {
            raf.writeUTF(t.name);
            raf.writeInt(t.numEntries);
        }
    }

}
