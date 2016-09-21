/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2015 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.http;

import static com.atomicleopard.expressive.Expressive.list;
import static com.threewks.thundr.http.URLEncoder.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.transformer.TransformerManager;

public class URLEncoderTest {
    @Test
    public void shouldEncodeQueryComponent() {
        // basic whitespace and empty values
        assertThat(encodeQueryComponent(null), is(nullValue()));
        assertThat(encodeQueryComponent(""), is(""));
        assertThat(encodeQueryComponent(" "), is("%20"));
        assertThat(encodeQueryComponent("  "), is("%20%20"));

        // regular sensible values
        assertThat(encodeQueryComponent("text"), is("text"));
        assertThat(encodeQueryComponent("text and more"), is("text%20and%20more"));

        // unreserved characters
        assertThat(encodeQueryComponent("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()"), is("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()"));

        // query reserved characters
        assertThat(encodeQueryComponent(";"), is("%3B"));
        assertThat(encodeQueryComponent("/"), is("%2F"));
        assertThat(encodeQueryComponent("?"), is("%3F"));
        assertThat(encodeQueryComponent(":"), is("%3A"));
        assertThat(encodeQueryComponent("@"), is("%40"));
        assertThat(encodeQueryComponent("&"), is("%26"));
        assertThat(encodeQueryComponent("="), is("%3D"));
        assertThat(encodeQueryComponent("+"), is("%2B"));
        assertThat(encodeQueryComponent(","), is("%2C"));
        assertThat(encodeQueryComponent(";"), is("%3B"));
        assertThat(encodeQueryComponent("#"), is("%23"));
        assertThat(encodeQueryComponent("$"), is("%24"));
        assertThat(encodeQueryComponent("%"), is("%25"));
        assertThat(encodeQueryComponent("^"), is("%5E"));
        assertThat(encodeQueryComponent("\\"), is("%5C"));
        assertThat(encodeQueryComponent("\""), is("%22"));
        assertThat(encodeQueryComponent(">"), is("%3E"));
        assertThat(encodeQueryComponent("<"), is("%3C"));
        assertThat(encodeQueryComponent(","), is("%2C"));
        assertThat(encodeQueryComponent("¡"), is("%A1"));
        assertThat(encodeQueryComponent("™"), is("%2122"));
        assertThat(encodeQueryComponent("£"), is("%A3"));
    }

    @Test
    public void shouldEncodePathComponent() {
        // basic whitespace and empty values
        assertThat(encodePathComponent(null), is(nullValue()));
        assertThat(encodePathComponent(""), is(""));
        assertThat(encodePathComponent(" "), is("%20"));
        assertThat(encodePathComponent("  "), is("%20%20"));

        // regular sensible values
        assertThat(encodePathComponent("text"), is("text"));
        assertThat(encodePathComponent("text and more"), is("text%20and%20more"));

        // unreserved characters
        assertThat(encodePathComponent("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()"), is("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()"));

        // path reserved characters- strictly speaking not all of these
        // need to be encoded, but encoding them is safer because they'll
        // always be decoded
        assertThat(encodePathComponent("/"), is("%2F"));
        assertThat(encodePathComponent("?"), is("%3F"));
        assertThat(encodePathComponent(":"), is("%3A"));
        assertThat(encodePathComponent("@"), is("%40"));
        assertThat(encodePathComponent("&"), is("%26"));
        assertThat(encodePathComponent("="), is("%3D"));
        assertThat(encodePathComponent("+"), is("%2B"));
        assertThat(encodePathComponent(","), is("%2C"));
        assertThat(encodePathComponent(";"), is("%3B"));
        assertThat(encodePathComponent("#"), is("%23"));
        assertThat(encodePathComponent("%"), is("%25"));
        assertThat(encodePathComponent("^"), is("%5E"));
        assertThat(encodePathComponent("\\"), is("%5C"));
        assertThat(encodePathComponent("\""), is("%22"));
        assertThat(encodePathComponent(">"), is("%3E"));
        assertThat(encodePathComponent("<"), is("%3C"));
        assertThat(encodePathComponent("¡"), is("%A1"));
        assertThat(encodePathComponent("™"), is("%2122"));
        assertThat(encodePathComponent("£"), is("%A3"));
    }

    @Test
    public void shouldEncodePathSlugComponent() {
        // basic whitespace and empty values
        assertThat(encodePathSlugComponent(null), is(nullValue()));
        assertThat(encodePathSlugComponent(""), is(""));
        assertThat(encodePathSlugComponent(" "), is("-"));
        assertThat(encodePathSlugComponent("  "), is("-"));
        assertThat(encodePathSlugComponent("PathContent"), is("PathContent"));
        assertThat(encodePathSlugComponent("Path Content"), is("Path-Content"));
        assertThat(encodePathSlugComponent("Path&Content"), is("Path-Content"));
        assertThat(encodePathSlugComponent("Path & Content"), is("Path-Content"));
        assertThat(encodePathSlugComponent("Path, and Content"), is("Path-and-Content"));
        assertThat(encodePathSlugComponent("Path's and Content"), is("Paths-and-Content"));
    }

    @Test
    public void shouldDecodePath() {
        String encoded1 = encodePathComponent("This is - some, stuff & ? more things");
        assertThat(encoded1, is(not("This is - some, stuff & ? more things")));
        assertThat(decodePathComponent(encoded1), is("This is - some, stuff & ? more things"));
        
        String encodedAll = encodePathComponent("This is **)()()@#!898492834dfkajd fkjd><\":}{}- some, stuff & ? more things");
        assertThat(encodedAll, is(not("This is **)()()@#!898492834dfkajd fkjd><\":}{}- some, stuff & ? more things")));
        assertThat(decodePathComponent(encodedAll), is("This is **)()()@#!898492834dfkajd fkjd><\":}{}- some, stuff & ? more things"));
    }

    @Test
    public void shouldDecodePathWhenPlusSymbolPresent() {
        // The plus symbol is an example of a path character that doesn't require
        // encoding, but can be. This test exists because if you use query parameter
        // encoding, it would be decoded to a space, which is incorrect
        String encodePathComponent = encodePathComponent("path+component");
        assertThat(encodePathComponent, is("path%2Bcomponent"));
        assertThat(decodePathComponent(encodePathComponent), is("path+component"));
        assertThat(decodePathComponent("path+component"), is("path+component"));
    }

    @Test
    public void shouldEncodeQueryParameters() {
        assertThat(encodeQueryString(paramMap("param1", "value1", "param2", 2)), is("?param1=value1&param2=2"));
        assertThat(encodeQueryString(paramMap("par am1", "val ue1", "param2", 2)), is("?par%20am1=val%20ue1&param2=2"));
        assertThat(encodeQueryString(paramMap("par=am1", "val=ue1", "param2", 2)), is("?par%3Dam1=val%3Due1&param2=2"));
        assertThat(encodeQueryString(paramMap("par&am1", "val&ue1", "param2", 2)), is("?par%26am1=val%26ue1&param2=2"));
        assertThat(encodeQueryString(paramMap("param1", "value1", "param2", null)), is("?param1=value1&param2="));
        assertThat(encodeQueryString(paramMap("param1", "value1", "param2", new DateTime(2014, 1, 30, 12, 0, 0, 0).withZoneRetainFields(DateTimeZone.UTC))),
                is("?param1=value1&param2=2014-01-30T12%3A00%3A00.000Z"));

        // basic null and empty values
        assertThat(encodeQueryString(null), is(""));
        assertThat(encodeQueryString(paramMap()), is(""));
    }

    @Test
    public void shouldEncodeQueryParametersUsingGivenTransformerManager() {
        TransformerManager transformerManager = TransformerManager.createEmpty();
        transformerManager.register(Integer.class, String.class, new ETransformer<Integer, String>() {
            @Override
            public String from(Integer from) {
                return "map+ped";
            }
        });

        assertThat(encodeQueryString(paramMap("param", 2), transformerManager), is("?param=map%2Bped"));
        assertThat(encodeQueryString(paramMap("param", 2, "param2", 5), transformerManager), is("?param=map%2Bped&param2=map%2Bped"));

        // basic null and empty values
        assertThat(encodeQueryString(null, transformerManager), is(""));
        assertThat(encodeQueryString(paramMap(), transformerManager), is(""));
    }

    @Test
    public void shouldDeccodeQueryParameters() {
        assertThat(decodeQueryString("?param1=value1&param2=2"), is(queryParamMap("param1", list("value1"), "param2", list("2"))));
        assertThat(decodeQueryString("?par%20am1=val%20ue1&param2=2"), is(queryParamMap("par am1", list("val ue1"), "param2", list("2"))));
        assertThat(decodeQueryString("?par%3Dam1=val%3Due1&param2=2"), is(queryParamMap("par=am1", list("val=ue1"), "param2", list("2"))));
        assertThat(decodeQueryString("?par%26am1=val%26ue1&param2=2"), is(queryParamMap("par&am1", list("val&ue1"), "param2", list("2"))));
        assertThat(decodeQueryString("?param1=value1&param2="), is(queryParamMap("param1", list("value1"), "param2", Collections.singletonList(null))));
        assertThat(decodeQueryString("?param1=value1&param2=2014-01-30T12%3A00%3A00.000Z"), is(queryParamMap("param1", list("value1"), "param2", list("2014-01-30T12:00:00.000Z"))));
        assertThat(decodeQueryString("param1=value1&param2=2"), is(queryParamMap("param1", list("value1"), "param2", list("2"))));
        assertThat(decodeQueryString("par%20am1=val%20ue1&param2=2"), is(queryParamMap("par am1", list("val ue1"), "param2", list("2"))));
        assertThat(decodeQueryString("par%3Dam1=val%3Due1&param2=2"), is(queryParamMap("par=am1", list("val=ue1"), "param2", list("2"))));
        assertThat(decodeQueryString("par%26am1=val%26ue1&param2=2"), is(queryParamMap("par&am1", list("val&ue1"), "param2", list("2"))));
        assertThat(decodeQueryString("param1=value1&param2="), is(queryParamMap("param1", list("value1"), "param2", Collections.singletonList(null))));
        assertThat(decodeQueryString("param1=value1&param2=2014-01-30T12%3A00%3A00.000Z"), is(queryParamMap("param1", list("value1"), "param2", list("2014-01-30T12:00:00.000Z"))));

        assertThat(decodeQueryString("param1=value1&param1=value2&param1="), is(queryParamMap("param1", list("value1", "value2", null))));

        // basic whitespace and empty values
        assertThat(decodeQueryString(null), is(queryParamMap()));
        assertThat(decodeQueryString(""), is(queryParamMap()));
        assertThat(decodeQueryString("?"), is(queryParamMap()));
    }

    private Map<String, List<String>> queryParamMap(Object... values) {
        return Expressive.map(values);
    }

    private Map<String, Object> paramMap(Object... values) {
        Map<String, Object> results = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            String key = (String) values[i];
            Object value = (Object) values[i + 1];
            results.put(key, value);
        }
        return results;
    }

}
