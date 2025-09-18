package org.meeuw.i18n.subdivision;

import java.util.*;

import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.regions.RegionService;

import static org.meeuw.i18n.subdivision.SubdivisionProvider.MAP;

/**
 * A few utility methods to work with {@link CountrySubdivision subdivisions}.
 *
 * @author Michiel Meeuwissen
 *
 */
public class SubdivisionFactory {

    /**
     * Get all subdivisions for a country. Or {@code null} if not known, not found, or not applicable
     * @param country Country to resolve for
     * @return List of subdivisions
     */

    public static List<CountrySubdivision> getSubdivisions(Country country) {
        List<CountrySubdivision> result = MAP.get(country);
        if (result == null) {
            return Collections.emptyList();
        } else {
            return result;
        }
    }

    /**
     * Retrieve all known subdivisions for a country, given the country code.
     *
     * @param country The country code to resolve for
     * @return List of subdivisions
     * @throws java.util.NoSuchElementException If the country is not found
     */
    public static List<CountrySubdivision> getSubdivisions(String country) {
        return getSubdivisions(RegionService.getInstance()
            .getByCode(country, true, Country.class)
            .orElseThrow(() -> new NoSuchElementException("No country found for " + country))
        );
    }

    /**
     * @param country Country to resolve for
     * @param subdivisionCodeName The code of the subdivision to resolve
     * @return The subdivision, if found, otherwise {@link Optional#empty()}
     */
    public static Optional<CountrySubdivision> getSubdivision(Country country, String subdivisionCodeName) {
        for (CountrySubdivision subDivisionCode: getSubdivisions(country)) {
            if (subDivisionCode.getSubdivisionCode().equals(subdivisionCodeName)) {
                return Optional.of(subDivisionCode);
            }
        }
        return Optional.empty();
    }

    /**
     * @param country Country to resolve for
     * @param subdivisionCodeName The code of the subdivision to resolve
     * @return The subdivision, if found, otherwise {@link Optional#empty()}
     */
    public static Optional<CountrySubdivision> getSubdivision(String country, String subdivisionCodeName) {
        return getSubdivision(RegionService.getInstance()
            .getByCode(country, true, Country.class)
            .orElseThrow(() -> new NoSuchElementException("No country found for " + country)), subdivisionCodeName);
    }
}
