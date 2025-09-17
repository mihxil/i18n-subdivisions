
package org.meeuw.i18n.subdivision;


import org.meeuw.i18n.countries.Country;
import org.meeuw.i18n.regions.Region;

/**
 *
 */
public interface CountryCodeSubdivision extends Region {


    @Override
    String getCode();

    @Override
    default Type getType() {
        return Type.SUBDIVISION;
    }
    Country  getCountry();

    boolean isRealRegion();

    String[] getSource();

}
