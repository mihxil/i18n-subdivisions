package org.meeuw.i18n.subdivision;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.neovisionaries.i18n.CountryCode;

class SubdivisionsTest {

    @Test
    public void showAll() throws URISyntaxException, IOException {
        StringBuilder builder = new StringBuilder();
        for (CountryCode cc : CountryCode.values()) {
            List<CountryCodeSubdivision> subdivisions = SubdivisionFactory.getSubdivisions(cc);
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(cc.name()).append("\t").append(cc.getName()).append("\n");
            //System.out.println("Found " + subdivision.getEnumConstants().length + " for " + cc);

            for (CountryCodeSubdivision sd : subdivisions) {
                builder.append("\t").append(sd.getCode()).append("\t").append(sd.getName()).append("\n");
            }
        }
        String expected = IOUtils.toString(getClass().getResource("/current.txt").toURI(), StandardCharsets.UTF_8);
        Assertions.assertEquals(expected, builder.toString());
    }

}
