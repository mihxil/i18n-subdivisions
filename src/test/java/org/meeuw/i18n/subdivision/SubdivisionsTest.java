package org.meeuw.i18n.subdivision;

import com.neovisionaries.i18n.CountryCode;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubdivisionsTest {

    @Test
    public void showAll() throws ClassNotFoundException, URISyntaxException, IOException {
        StringBuilder builder = new StringBuilder();
        for (CountryCode cc : CountryCode.values()) {
            Class<?> subdivision = Class.forName("org.meeuw.i18n.subdivision.Subdivision" + cc.name());
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(cc.getName()).append("\n");
            //System.out.println("Found " + subdivision.getEnumConstants().length + " for " + cc);

            for (Object e : subdivision.getEnumConstants()) {
                CountryCodeSubdivision sd = (CountryCodeSubdivision) e;
                builder.append("\t").append(sd.getCode()).append("\t").append(sd.getName()).append("\n");
            }
        }
        String expected = Files.readString(
            Paths.get(getClass().getResource("/current.txt").toURI()), StandardCharsets.UTF_8);
        Assertions.assertEquals(expected, builder.toString());
    }

}
