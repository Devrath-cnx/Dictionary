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

import com.cnx.dictionarytool.library.others.CollatorWrapper;
import com.cnx.dictionarytool.library.others.DictionaryApplication;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Language {

    public static final class LanguageResources {
        final String englishName;
        public final int nameId;
        public final int flagId;

        public LanguageResources(final String englishName, int nameId, int flagId) {
            this.englishName = englishName;
            this.nameId = nameId;
            this.flagId = flagId;
        }

        public LanguageResources(final String englishName, int nameId) {
            this(englishName, nameId, 0);
        }
    }

    private static final Map<String, Language> registry = new HashMap<>();

    private final String isoCode;
    private final Locale locale;

    private Language(final Locale locale, final String isoCode) {
        this.locale = locale;
        this.isoCode = isoCode;

        registry.put(isoCode.toLowerCase(), this);
    }

    @Override
    public String toString() {
        return locale.toString();
    }

    public String getIsoCode() {
        return isoCode;
    }

    public synchronized Comparator<Object> getCollator() {
        if (!DictionaryApplication.USE_COLLATOR)
            return new Comparator<Object>() {
                @Override
                public int compare(Object o, Object t1) {
                    return String.class.cast(o).compareToIgnoreCase(String.class.cast(t1));
                }
            };
        // TODO: consider if this should be cached - but must be thread-safe
        return CollatorWrapper.getInstanceStrengthIdentical(locale);
    }

    public String getDefaultNormalizerRules() {
        return ":: Any-Latin; ' ' > ; :: Lower; :: NFD; :: [:Nonspacing Mark:] Remove; :: NFC ;";
    }

    /**
     * A practical pattern to identify strong RTL characters. This pattern is
     * not completely correct according to the Unicode standard. It is
     * simplified for performance and small code size.
     */
    private static final String rtlChars =
        "\u0591-\u07FF\uFB1D-\uFDFD\uFE70-\uFEFC";

    @SuppressWarnings("unused")
    public static String fixBidiText(String text) {
        // TODO: RTL text (e.g. arabic) in parenthesis might need extra
        // \u200e markers sometimes - check what exactly is going on there.
        return text;
    }

    // ----------------------------------------------------------------

    public static final Language en = new Language(Locale.ENGLISH, "EN");
    public static final Language it = new Language(Locale.ITALIAN, "IT");

    public static final Language de = new Language(Locale.GERMAN, "DE") {
        @Override
        public String getDefaultNormalizerRules() {
            return ":: Lower; 'ae' > 'ä'; 'oe' > 'ö'; 'ue' > 'ü'; 'ß' > 'ss'; ";
        }
    };

    // ----------------------------------------------------------------

    public static synchronized Language lookup(final String isoCode) {
        Language lang = registry.get(isoCode.toLowerCase());
        if (lang == null) {
            lang = new Language(new Locale(isoCode), isoCode);
        }
        return lang;
    }

}
