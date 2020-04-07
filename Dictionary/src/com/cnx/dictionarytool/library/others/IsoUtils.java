// Copyright 2011 Google Inc. All Rights Reserved.
// Copyright 2017 Reimar Döffinger. All Rights Reserved.
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

package com.cnx.dictionarytool.library.others;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cnx.dictionarytool.R;
import com.cnx.dictionarytool.library.others.DictionaryInfo.IndexInfo;
import com.cnx.dictionarytool.library.engine.Language.LanguageResources;

public enum IsoUtils {
    INSTANCE;

    // Useful:
    // http://www.loc.gov/standards/iso639-2/php/code_list.php
    private final Map<String, LanguageResources> isoCodeToResources = new HashMap<>();
    IsoUtils() {
        isoCodeToResources.put("AF", new LanguageResources("Afrikaans", R.string.AF,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("SQ", new LanguageResources("Albanian", R.string.SQ,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("AR",
                               new LanguageResources("Arabic", R.string.AR, R.drawable.ic_icon_search));
        isoCodeToResources.put("HY", new LanguageResources("Armenian", R.string.HY,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("BE", new LanguageResources("Belarusian", R.string.BE,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("BN", new LanguageResources("Bengali", R.string.BN));
        isoCodeToResources.put("BS", new LanguageResources("Bosnian", R.string.BS,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("BG", new LanguageResources("Bulgarian", R.string.BG,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("MY", new LanguageResources("Burmese", R.string.MY,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("ZH", new LanguageResources("Chinese", R.string.ZH,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("cmn", new LanguageResources("Mandarin", R.string.cmn,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("yue", new LanguageResources("Cantonese", R.string.yue,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("CA", new LanguageResources("Catalan", R.string.CA));
        isoCodeToResources.put("HR", new LanguageResources("Croatian", R.string.HR,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("CS", new LanguageResources("Czech", R.string.CS,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("DA", new LanguageResources("Danish", R.string.DA,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("NL", new LanguageResources("Dutch", R.string.NL,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("EN", new LanguageResources("English", R.string.EN,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("EO", new LanguageResources("Esperanto", R.string.EO,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("ET", new LanguageResources("Estonian", R.string.ET,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("FI", new LanguageResources("Finnish", R.string.FI,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("FR", new LanguageResources("French", R.string.FR,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("DE", new LanguageResources("German", R.string.DE,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("EL", new LanguageResources("Greek", R.string.EL,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("grc", new LanguageResources("Ancient Greek", R.string.grc));
        isoCodeToResources.put("haw", new LanguageResources("Hawaiian", R.string.haw,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("HE", new LanguageResources("Hebrew", R.string.HE,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("HI", new LanguageResources("Hindi", R.string.HI, R.drawable.ic_icon_search));
        isoCodeToResources.put("HU", new LanguageResources("Hungarian", R.string.HU,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("IS", new LanguageResources("Icelandic", R.string.IS,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("ID", new LanguageResources("Indonesian", R.string.ID,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("GA", new LanguageResources("Irish", R.string.GA,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("GD", new LanguageResources("Scottish Gaelic", R.string.GD,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("GV", new LanguageResources("Manx", R.string.GV,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("IT", new LanguageResources("Italian", R.string.IT,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("LA", new LanguageResources("Latin", R.string.LA));
        isoCodeToResources.put("LV", new LanguageResources("Latvian", R.string.LV,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("LT", new LanguageResources("Lithuanian", R.string.LT,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("JA", new LanguageResources("Japanese", R.string.JA,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("KO", new LanguageResources("Korean", R.string.KO,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("KU", new LanguageResources("Kurdish", R.string.KU));
        isoCodeToResources.put("MS", new LanguageResources("Malay", R.string.MS,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("MI", new LanguageResources("Maori", R.string.MI,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("MN", new LanguageResources("Mongolian", R.string.MN,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("NE", new LanguageResources("Nepali", R.string.NE,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("NO", new LanguageResources("Norwegian", R.string.NO,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("FA", new LanguageResources("Persian", R.string.FA,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("PL", new LanguageResources("Polish", R.string.PL,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("PT", new LanguageResources("Portuguese", R.string.PT,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("PA", new LanguageResources("Punjabi", R.string.PA));
        isoCodeToResources.put("RO", new LanguageResources("Romanian", R.string.RO,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("RU", new LanguageResources("Russian", R.string.RU,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("SA", new LanguageResources("Sanskrit", R.string.SA));
        isoCodeToResources.put("SR", new LanguageResources("Serbian", R.string.SR,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("SK", new LanguageResources("Slovak", R.string.SK,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("SL", new LanguageResources("Slovenian", R.string.SL,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("SO", new LanguageResources("Somali", R.string.SO,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("ES", new LanguageResources("Spanish", R.string.ES,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("SW", new LanguageResources("Swahili", R.string.SW));
        isoCodeToResources.put("SV", new LanguageResources("Swedish", R.string.SV,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("TL", new LanguageResources("Tagalog", R.string.TL));
        isoCodeToResources.put("TG", new LanguageResources("Tajik", R.string.TG,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("TH", new LanguageResources("Thai", R.string.TH,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("BO", new LanguageResources("Tibetan", R.string.BO));
        isoCodeToResources.put("TR", new LanguageResources("Turkish", R.string.TR,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("UK", new LanguageResources("Ukrainian", R.string.UK,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("UR", new LanguageResources("Urdu", R.string.UR));
        isoCodeToResources.put("VI", new LanguageResources("Vietnamese", R.string.VI,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("CI", new LanguageResources("Welsh", R.string.CI,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("YI", new LanguageResources("Yiddish", R.string.YI));
        isoCodeToResources.put("ZU", new LanguageResources("Zulu", R.string.ZU));
        isoCodeToResources.put("AZ", new LanguageResources("Azeri", R.string.AZ,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("EU", new LanguageResources("Basque", R.string.EU,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("BR", new LanguageResources("Breton", R.string.BR));
        isoCodeToResources.put("MR", new LanguageResources("Marathi", R.string.MR));
        isoCodeToResources.put("FO", new LanguageResources("Faroese", R.string.FO));
        isoCodeToResources.put("GL", new LanguageResources("Galician", R.string.GL,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("KA", new LanguageResources("Georgian", R.string.KA,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("HT", new LanguageResources("Haitian Creole", R.string.HT,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("LB", new LanguageResources("Luxembourgish", R.string.LB,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("MK", new LanguageResources("Macedonian", R.string.MK,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("LO", new LanguageResources("Lao", R.string.LO,
                               R.drawable.ic_icon_search));
        isoCodeToResources.put("ML", new LanguageResources("Malayalam", R.string.ML));
        isoCodeToResources.put("TA", new LanguageResources("Tamil", R.string.TA));
        isoCodeToResources.put("SH", new LanguageResources("Serbo-Croatian", R.string.SH));
        isoCodeToResources.put("SD", new LanguageResources("Sindhi", R.string.SD));

        // Hack to allow lower-case ISO codes to work:
        for (final String isoCode : new ArrayList<>(isoCodeToResources.keySet())) {
            isoCodeToResources.put(isoCode.toLowerCase(), isoCodeToResources.get(isoCode));
        }
    }



    public String isoCodeToLocalizedLanguageName(final Context context, final String isoCode) {
        String lang = new Locale(isoCode).getDisplayLanguage();
        if (!lang.equals("") && !lang.equals(isoCode))
        {
            return lang;
        }
        final LanguageResources languageResources = isoCodeToResources.get(isoCode);
        if (languageResources != null)
        {
            lang = context.getString(languageResources.nameId);
        }
        return lang;
    }



}
