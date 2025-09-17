
package org.meeuw.i18n.subdivision;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.regions.RegionService;
import org.meeuw.i18n.regions.spi.RegionProvider;

public final class SubdivisionFactory implements RegionProvider<CountryCodeSubdivision> {

    private static final Map<Country, List<CountryCodeSubdivision>> map = new ConcurrentHashMap<>();


    public void registerSubdivisions(Country countryCode, List<CountryCodeSubdivision> subdivisions) {
        if (subdivisions == null || subdivisions.isEmpty()) {
            return;
        }
        map.put(countryCode, Collections.unmodifiableList(subdivisions));
    }

    /**
     * Get all subdivisions for a country. Or {@code null} if not known, not found, or not applicable
     * 
     */
    @Nullable
    public static List<CountryCodeSubdivision> getSubdivisions(Country countryCode) {
        return map.get(countryCode);
    }

    public static List<CountryCodeSubdivision> getSubdivisions(String countryCode) {
        return map.get(RegionService.getInstance().getByCode(countryCode, true, Country.class).get());
    }

    public static CountryCodeSubdivision getSubdivision(Country countryCode, String subdivisionCodeName) {
        for (CountryCodeSubdivision subDivisionCode: map.get(countryCode)) {
            if (subDivisionCode.getCode().equals(subdivisionCodeName)) {
                return subDivisionCode;
            }
        }
        return null;
    }

    @Override
    public Class<CountryCodeSubdivision> getProvidedClass() {
        return CountryCodeSubdivision.class;
    }

    @Override
    public Stream<CountryCodeSubdivision> values() {
        return map.values().stream().flatMap(Collection::stream);
    }
}
