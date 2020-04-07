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

import com.cnx.dictionarytool.library.raf.RAFListSerializer;
import com.cnx.dictionarytool.library.raf.RAFSerializable;
import com.ibm.icu.text.Transliterator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class TextEntry extends AbstractEntry implements RAFSerializable<TextEntry> {

    private final String text;

    private TextEntry(final Dictionary dictionary, final DataInput raf, final int index)
    throws IOException {
        super(dictionary, raf, index);
        text = raf.readUTF();
        throw new RuntimeException("TextEntry constructor should be unreachable");
    }

    @Override
    public void write(DataOutput raf) throws IOException {
        super.write(raf);
        raf.writeUTF(text);
    }

    static final class Serializer implements RAFListSerializer<TextEntry> {

        final Dictionary dictionary;

        Serializer(Dictionary dictionary) {
            this.dictionary = dictionary;
        }

        @Override
        public TextEntry read(DataInput raf, final int index) throws IOException {
            return new TextEntry(dictionary, raf, index);
        }

        @Override
        public void write(DataOutput raf, TextEntry t) throws IOException {
            t.write(raf);
        }
    }

    @Override
    public void addToDictionary(final Dictionary dictionary) {
        assert index == -1;
        dictionary.textEntries.add(this);
        index = dictionary.textEntries.size() - 1;
    }

    @Override
    public RowBase CreateRow(int rowIndex, Index dictionaryIndex) {
        throw new UnsupportedOperationException("TextEntry's don't really exist.");
    }

    public static class Row extends RowBase {

        Row(final DataInput raf, final int thisRowIndex,
            final Index index, int extra) throws IOException {
            super(raf, thisRowIndex, index, extra);
        }

        TextEntry getEntry() {
            return index.dict.textEntries.get(referenceIndex);
        }

        @Override
        public void print(PrintStream out) {
            out.println("  " + getEntry().text);
        }

        @Override
        public String getRawText(boolean compact) {
            return getEntry().text;
        }

        @Override
        public RowMatchType matches(final List<String> searchTokens,
                                    final Pattern orderedMatchPattern, Transliterator normalizer,
                                    boolean swapPairEntries) {
            return null;
        }
    }

}
