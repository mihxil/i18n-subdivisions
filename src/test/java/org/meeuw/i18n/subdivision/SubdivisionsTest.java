package org.meeuw.i18n.subdivision;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.countries.UserAssignedCountry;
import org.meeuw.i18n.regions.Region;
import org.meeuw.i18n.regions.RegionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SubdivisionsTest {

    @Test
    public void showAll() throws URISyntaxException, IOException {
        StringBuilder builder = new StringBuilder();
        RegionService.getInstance(SubdivisionsTest.class.getClassLoader()).values(Country.class)
            .sorted(Comparator.comparing(Country::getCode))
            .forEach(country -> {
                if (builder.length() > 0) {
                    builder.append("\n");
                }
                builder.append(country.getCode()).append("\t").append(country.getName()).append("\n");
                //System.out.println("Found " + subdivision.getEnumConstants().length + " for " + cc);

                List<CountrySubdivision> subdivisions = SubdivisionFactory.getSubdivisions(country);
                if (subdivisions != null) {
                    for (CountrySubdivision sd : subdivisions) {
                        builder.append("\t").append(sd.getSubdivisionCode()).append("\t").append(sd.getName());
                        assertThat(sd.getType()).isEqualTo(Region.Type.SUBDIVISION);
                        assertThat(sd.getSource()).isNotNull();
                        if (!sd.isRealRegion()) {
                            builder.append("\tPLACEHOLDER");
                        }
                        builder.append("\n");

                    }
                }
        });
        String expected = IOUtils.toString(getClass().getResource("/current.txt").toURI(), StandardCharsets.UTF_8);
        Assertions.assertEquals(expected, builder.toString());
    }

    // tag::utrechtRegionService[]
    @Test
    public void utrechtRegionService() {
        CountrySubdivision u = RegionService.getInstance().getByCode("NL-UT", true, CountrySubdivision.class).get();

        assertThat(u.getCountry().getCode()).isEqualTo("NL");
        assertThat(u.getName()).isEqualTo("Utrecht");
    }

    // end::utrechtRegionService[]


    // tag::utrechtRegionFactory[]

    @Test
    public void utrechtFactory() {
        CountrySubdivision u = SubdivisionFactory.getSubdivision("NL", "UT").get();

        assertThat(u.getCountry().getCode()).isEqualTo("NL");
        assertThat(u.getName()).isEqualTo("Utrecht");
    }
    // end::utrechtRegionFactory[]



    // tag::belgium[]

    @Test
    public void belgium() {
        for (CountrySubdivision code : SubdivisionFactory.getSubdivisions("BE")) {
            System.out.format("[%s] %s\n", code, code.getName());
        }

    }
    // end::belgium[]



    // tag::greatbritain[]

    @Test
    public void greatBritain() {
        Country gb = RegionService.getInstance().getByCode("GB", Country.class).get();

        for (CountrySubdivision code : SubdivisionFactory.getSubdivisions(gb)) {
            System.out.format("[%s] %s\n", code, code.getName());
        }

        CountrySubdivision wales = RegionService.getInstance().getByCode("GB-WLS", CountrySubdivision.class).get();
        assertThat(wales.getCountry()).isEqualTo(gb);
        assertThat(wales.getName()).isEqualTo("Wales");

    }
    // end::greatbritain[]



    @Test
    public void notFound() {
        assertThatThrownBy(() -> SubdivisionFactory.getSubdivisions("XX")).isInstanceOf(java.util.NoSuchElementException.class);
        assertThat(SubdivisionFactory.getSubdivision("NL", "XX")).isEmpty();
        assertThat(SubdivisionFactory.getSubdivision(new MyCountrySubdivision("foo", "bar", "me"), "XX") ).isEmpty();
    }

    static class MyCountrySubdivision extends UserAssignedCountry {
        MyCountrySubdivision(String code, String name, String assignedBy) {
            super(code, name, assignedBy);
        }
    }

}
