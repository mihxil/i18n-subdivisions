package org.meeuw.i18n.subdivisions.codes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.countries.UserAssignedCountry;
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

                SubdivisionFactory.streamSubdivisions(country.getCode()).forEach(sub -> {
                    builder.append("\t").append(sub.getSubdivisionCode()).append("\t").append(sub.getName());
                    if (!sub.isRealRegion()) {
                       builder.append("\tPLACEHOLDER");
                    }
                    builder.append("\n");
                });


        });
        String expected = IOUtils.toString(getClass().getResource("/current.txt").toURI(), StandardCharsets.UTF_8);
        Assertions.assertEquals(expected, builder.toString());
    }

    // tag::utrecht[]
    @Test
    public void utrecht() {

        CountrySubdivisionCode u = SubdivisionNL.valueOf("UT");

        assertThat(u.getCountryCode()).isEqualTo("NL");
        assertThat(u.getName()).isEqualTo("Utrecht");
    }

    // end::utrecht[]


    // tag::utrechtRegionFactory[]

    @Test
    public void utrechtFactory() {
        CountrySubdivisionCode u = SubdivisionFactory.getSubdivision("NL", "UT").get();

        assertThat(u.getCountryCode()).isEqualTo("NL");
        assertThat(u.getName()).isEqualTo("Utrecht");
    }
    // end::utrechtRegionFactory[]



    // tag::belgium[]

    @Test
    public void belgium() {
        for (CountrySubdivisionCode code : SubdivisionFactory.getSubdivisions("BE")) {
            System.out.format("[%s] %s\n", code, code.getName());
        }

    }
    // end::belgium[]



    // tag::greatbritain[]

    @Test
    public void greatBritain() {

        for (CountrySubdivisionCode code : SubdivisionFactory.getSubdivisions("GB")) {
            System.out.format("[%s] %s\n", code, code.getName());
        }



    }
    // end::greatbritain[]



    @Test
    public void notFound() {
        assertThatThrownBy(() -> SubdivisionFactory.getSubdivisions("XX")).isInstanceOf(java.util.NoSuchElementException.class);
        assertThat(SubdivisionFactory.getSubdivision("NL", "XX")).isEmpty();

    }

    @Test
    public void stream() {
        assertThat(SubdivisionFactory.stream().count()).isEqualTo(5869L);
    }

    static class MyCountrySubdivision extends UserAssignedCountry {
        MyCountrySubdivision(String code, String name, String assignedBy) {
            super(code, name, assignedBy);
        }
    }

}
