#!/usr/bin/env groovy
import com.sun.codemodel.*
import groovy.transform.Field
import jakarta.annotation.Generated
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import org.jsoup.select.Elements
import org.meeuw.i18n.countries.Country
import org.meeuw.i18n.regions.RegionService
import org.meeuw.i18n.subdivision.CountryCodeSubdivision
import org.meeuw.i18n.subdivision.SubdivisionFactory

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4

@Field
final String JAVA_PACKAGE = "org.meeuw.i18n.subdivision"

@Field
final JCodeModel cm = new JCodeModel()
@Field
final JClass countryClass = cm.ref(Country.class)
@Field
final JClass countrySubdivisionInterface = cm.ref(CountryCodeSubdivision.class)

@Field
final JClass factory = cm.ref(SubdivisionFactory.class)

@Field
final Safelist safelist = Safelist.relaxed()
    .addAttributes("table", "class")
    .removeAttributes("a", "title")

class SubDiv {
    String code
    String name
    String row
    final List<String> source = new ArrayList<>()
}



Map<String, SubDiv> parseHtmlWiki(Country cc, URL uri, URL sourceUrl) {
    System.out.println("Parsing for " + uri)
    Map<String, SubDiv> parsedData = new TreeMap<>();
    try {
        def html = uri.getText("UTF-8")
        def unclean = Jsoup.parse(html)
        unclean.select("img").forEach(e -> {
            URI src = new URI(e.attr("src"))
            e.attr("alt", "flag for " + cc.getName())
            if (src.scheme == null) {
                e.attr("src", "https:" + e.attr("src"))
            }
        })
        Document parse = new Cleaner(safelist).clean(unclean)

        Elements rows = parse.select("table.wikitable > tbody > tr:gt(0)")
        rows.each { row ->
            //System.out.println("" + row)
            def newCode = row.select("> td:nth-child(1) > span").text()
            //System.out.println("" + newCode)
            if (newCode != null && newCode != "") {
                String[] parts = newCode.split('-', 2)
                if (parts.length == 1) {
                    parts = [cc.name(), parts[0]]
                }
                def newCC = RegionService.getInstance().getByCode(parts[0], false, Country.class).get()

                if (newCC != null && cc != null && cc != newCC) {
                    throw new IllegalArgumentException("For ${uri}, expected (Country=${cc}) but found (Country=${newCC})")
                } else {
                    // some fuzzy logic, not every wiki page is the same
                    def subDivisionCode = parts[1]


                    def link = null
                    // Try to guess which column contains the name
                    
                    for (s in [
                        "> td:nth-child(2) > a",
                        "> td:nth-child(2) a",
                        "> td:nth-child(3) > a",
                        "> td:nth-child(3) a",
                        "> td:nth-child(2)"]) {
                        link = row.select(s)
                        if (link != null && link.size() > 0 && StringUtils.isNotBlank(link.get(0).text())) {
                            break
                        }
                    }
                    def subDivisionName = trim(link.text())
                    if (subDivisionName == "") {
                       System.err.println("Name not found in " + uri + "\n"+ row + "(" + newCode + ":" + link + ")" )
                    } else {
                        SubDiv sub = new SubDiv()
                        sub.code = subDivisionCode
                        sub.name= subDivisionName
                        sub.source.add(sourceUrl.text)
                        sub.row = row.html()
                        parsedData[subDivisionCode] = sub
                    }
                }
            }
        }
    } catch (FileNotFoundException ignored) {
        System.out.println("Not found " + uri)
    }
    return parsedData
}

JClass parseHtml(Country cc, URL wikiUri, URL wikiSourceUri) {
    Map<String, SubDiv> parsedData = [:]

    try {
        parseHtmlWiki(cc, wikiUri, wikiSourceUri).forEach { k, v ->
            if (!parsedData.containsKey(k)) {
                //System.out.println("Found in wiki, but not in unece " + cc + " " + k + " = " + v)
                parsedData.put(k, v)
            } else {
                SubDiv subDiv = parsedData.get(k)
                subDiv.source.add(wikiSourceUri.text)
                subDiv.row = v.row
            }
        }
        generateClass(cc, parsedData)
    } catch (Exception e) {
        System.out.println("Skipped " + wikiUri + " " + e.getMessage())
    }
}


JClass generateClass(Country co, Map<String, SubDiv> parsedData) {
    JDefinedClass dc = cm._class(JMod.PUBLIC, "${JAVA_PACKAGE}.Subdivision${co.getCode()}", ClassType.ENUM)
    dc._implements(countrySubdivisionInterface)
      dc.annotate(Generated.class).param("value", this.class.getName())
      JDocComment classDoc = dc.javadoc()
      classDoc.append("<p>Subdivisions of {@link " + co.class.getName() + "#" + co.getName() + "} (" + co.getName() + ")</p>")

    JFieldVar name = dc.field(JMod.PRIVATE | JMod.FINAL, String.class, "name")
    JFieldVar code = dc.field(JMod.PRIVATE | JMod.FINAL, String.class, "code")
    JFieldVar source = dc.field(JMod.PRIVATE | JMod.FINAL, String[].class, "source")
    JFieldVar country = dc.field(JMod.PRIVATE | JMod.FINAL, String[].class, "country")

    dc.method(JMod.PUBLIC, Country.class, "getCountry").with {
        annotate(Override.class)
        body().with {
            _return(country)
        }
    }

    dc.method(JMod.PUBLIC, String.class, "getCode").with {
        annotate(Override.class)
        body().with {
            _return(code)
        }
    }

    dc.method(JMod.PUBLIC, String.class, "getName").with {
        annotate(Override.class)
        body().with {
            _return(name)
        }
    }

    dc.method(JMod.PUBLIC, String[].class, "getSource").with {
        annotate(Override.class)
        body().with {
            _return(source)
        }
    }

    dc.constructor(0).with {
        def subDivName = param(String.class, "subDivisionName")
        def subDivCode = param(String.class, "subDivisionCode")
        def subCountryCode = param(String.class, "countryCode")
        def subDivSource = varParam(String.class, "subDivisionSource")

        body().with {
            assign(JExpr._this().ref(name), subDivName)
            assign(JExpr._this().ref(code), subDivCode)
            assign(JExpr._this().ref(source), subDivSource)
            directStatement(
                    "this.country=RegionService.getInstance().getByCode(subCountryCode, false, Country.class));"
            )

        }
    }


    if (parsedData && parsedData.size() > 0) {
        boolean addedToClass = false
        parsedData.values().each { subDiv ->
            String escapedCode = subDiv.code
            if (Character.valueOf(escapedCode.charAt(0)).isDigit()) {
                escapedCode = ("_" + escapedCode).replaceAll("-", "_")
            }
            dc.enumConstant(escapedCode).with {
                arg(JExpr.lit(subDiv.name))
                arg(JExpr.lit(subDiv.code))
                for (String  so : subDiv.source) {
                    if (! addedToClass) {
                        classDoc.append("\n@see <a href='" + so + "'>" + so + "</a>")
                    }
                    arg(JExpr.lit(so))
                }
                addedToClass = true
                JDocComment constantDoc = javadoc()
                constantDoc.append(escapeHtml4(subDiv.name))
                if (subDiv.row != null && subDiv.row != "") {
                    constantDoc.append("\n<table><caption>" + escapeHtml4(subDiv.name) + "</caption><tr>" + subDiv.row + "</tr></table>\n")
                }
            }
        }
        
        dc.method(JMod.PUBLIC, boolean.class, "isRealRegion").with {
            annotate(Override)
            body().with {
                _return(JExpr.lit(true))
            }
        }
    } else {
        classDoc.append("<p>There are no known subdivisions of " + countryCode.getName() + "</p>")

        dc.enumConstant("NA").with {
            arg(JExpr.lit("No Subdivisions"))
            arg(JExpr.lit("NA"))
            arg(JExpr._null())
                     JDocComment constantDoc = javadoc()
                     constantDoc.append("<p>Place holder enum value. No known subdivisions for " + countryCode.name() + "</p>")
                }
        dc.method(JMod.PUBLIC, boolean.class, "isRealRegion").with {
            body().with {
                _return(JExpr.lit(false))
            }
        }
    }
    return dc
}

static String trim(String str) {
    StringUtils.trim(StringUtils.normalizeSpace(str))
}

Map<Country, JClass> classes = [:]
  RegionService.getInstance().values(Country.class).each {
    try {
        dir = "${project.properties.buildresources}"
    } catch (MissingPropertyException pe) {
        dir = "/Users/michiel/github/mihxil/i18n-subdivisions/src/build/resources/"
    }
    classes[it] = parseHtml(it,
        new URL("file://${dir}${it.code}.wiki.html"),
        new URL("file://${dir}${it.code}.wiki.url")
    )
}


def outputDir = new File(project.properties["subdivision.java.sources"] as String)
outputDir.mkdirs()
cm.build(outputDir)
