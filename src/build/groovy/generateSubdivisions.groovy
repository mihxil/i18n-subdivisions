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
import org.meeuw.i18n.subdivision.CountrySubdivisionCode

import java.util.stream.Collectors

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4

@Field
final String JAVA_PACKAGE = "org.meeuw.i18n.subdivision"

@Field
final JCodeModel cm = new JCodeModel()

@Field
final JClass countrySubdivisionInterface = cm.ref(CountrySubdivisionCode.class)


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



Map<String, SubDiv> parseHtmlWiki(String cc, URI uri, String sourceUrl) {
    //System.out.println("Parsing for " + uri)
    Map<String, SubDiv> parsedData = new TreeMap<>();
    try {
        def html = uri.toURL().getText("UTF-8")
        def unclean = Jsoup.parse(html)
        unclean.select("img").forEach(e -> {
            URI src = new URI(e.attr("src"))
            e.attr("alt", "flag for " + cc)
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
                    parts = [cc.getCode(), parts[0]]
                }

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
                    sub.source.add(sourceUrl.split("\t")[0])
                    sub.row = row.html()
                    parsedData[subDivisionCode] = sub
                }
            }

        }
    } catch (FileNotFoundException ignored) {
        System.out.println("Not found " + uri)
    }
    return parsedData
}

JClass parseHtml(String cc, URI wikiUri, String wikiSourceInfo) {
    Map<String, SubDiv> parsedData = [:]

    try {
        String[] wikiSourceInfoArray = wikiSourceInfo.split("\t");
        String wikiSourceUri = wikiSourceInfoArray[0]
        String countryName = wikiSourceInfoArray[3]
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

        generateClass(cc, countryName, parsedData)
    } catch (Exception e) {
        System.out.println("Skipped " + wikiUri + " " + e.getClass() + " " + e.getMessage())
        e.printStackTrace()
    }
}


JClass generateClass(String co,String countryname, Map<String, SubDiv> parsedData) {

    JDefinedClass dc = cm._class(JMod.PUBLIC, "${JAVA_PACKAGE}.Subdivision${co}", ClassType.ENUM)

    dc._implements(countrySubdivisionInterface)
      dc.annotate(Generated.class).param("value", this.class.getName())
      JDocComment classDoc = dc.javadoc()
      classDoc.append("<p>Subdivisions of {@code " + co  + "} " + countryname + "</p>")

    JFieldVar name = dc.field(JMod.PRIVATE | JMod.FINAL, String.class, "name")
    JFieldVar code = dc.field(JMod.PRIVATE | JMod.FINAL, String.class, "code")
    JFieldVar source = dc.field(JMod.PRIVATE | JMod.FINAL, String[].class, "source")

    dc.method(JMod.PUBLIC, String.class, "getCountryCode").with {
        annotate(Override.class)
        body().with {
            _return(JExpr.lit(co))
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
        def subDivSource = varParam(String.class, "subDivisionSource")


        body().with {
            assign(JExpr._this().ref(name), subDivName)
            assign(JExpr._this().ref(code), subDivCode)
            assign(JExpr._this().ref(source), subDivSource)

        }
    }


    dc.method(JMod.PUBLIC, String.class, "getSubdivisionCode").with {
        annotate(Override)
        body().with {
            _return(code)
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
        classDoc.append("<p>There are no known subdivisions of " + countryname + "</p>")

        dc.enumConstant("NA").with {
            arg(JExpr.lit("No Subdivisions"))
            arg(JExpr.lit("NA"))
            JDocComment constantDoc = javadoc()
            constantDoc.append("<p>Placeholder enum value. No known subdivisions for " +countryname + "</p>")
                }
        dc.method(JMod.PUBLIC, boolean.class, "isRealRegion").with {
            annotate(Override)
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

def dirPath;
def userDir = System.getProperty('user.dir')

try {
    dirPath = "${project.properties.buildresources}"
} catch (MissingPropertyException pe) {
    dirPath = userDir + "/src/build/resources/"
}
def dir = new File(dirPath)

Map<String, JClass> existing = new HashMap<>()

        //.filter(c -> c.code == "EU")
dir.eachFile { file ->

    def name = file.name
    if (name.endsWith('wiki.url')) {
        def code = name[0..-('wiki.url'.length() + 2)] // Removes '.wiki.url'
        existing.put(code, parseHtml(code,
                URI.create("file://${dir}/${code}.wiki.html"),
                new File("${dir}/${code}.wiki.url").getText("UTF-8")
        ))
    }
}



def outputDir;
try {

    outputDir = new File(project.properties["subdivision.java.sources"] as String)
} catch (MissingPropertyException pe) {
    outputDir = new File(userDir + "/src/target/generated-sources/subdivisions/")
}
outputDir.mkdirs()
cm.build(outputDir)

new File(userDir, "/src/main/resources/org/meeuw/i18n/subdivision/LIST").withWriter( writer -> {writer.write(existing.keySet().stream().collect(Collectors.joining("\t")))});
System.out.println("ready")

