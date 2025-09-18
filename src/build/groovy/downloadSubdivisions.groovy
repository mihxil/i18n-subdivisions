import org.meeuw.i18n.countries.Country
import org.meeuw.i18n.regions.RegionService

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

String dir;
 try {
        dir = "${project.properties.buildresources}"
    } catch (MissingPropertyException pe) {
        dir = "/Users/michiel/github/mihxil/i18n-subdivisions/src/build/resources/"
    }
File buildResources = new File(dir)
buildResources.mkdirs()


HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();



RegionService.getInstance().values(Country.class).each {

    if (false) {
        URI url = URI.create("https://unece.org/fileadmin/DAM/cefact/locode/Subdivision/${it.alpha2.toLowerCase(Locale.US)}Sub.htm")

        String html;
        try {
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))

            // seems to broken. All urls are giving 404 nowadays
            html = url.getText(["user-agent": "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.64 Safari/537.36"], "UTF-8")
        } catch (IOException e) {
            println(e.getMessage())
        }
        if (html) {
            new File(buildResources, "${it.alpha2}.html").withWriter("UTF-8", {
                it.write(html)
            })
            new File(buildResources, "${it.alpha2}.url").withWriter("UTF-8", {
                it.write(url.toExternalForm())
            })
        }
    }
    URI wikiUrl = URI.create("https://en.wikipedia.org/wiki/ISO_3166-2:" + it.code)
    //System.out.println(wikiUrl);
    String wikiHtml;
    int status;
    Instant lastModified;
    try {
        //https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_User-Agent_Policy
        HttpRequest request = HttpRequest.newBuilder(wikiUrl)
                .header("User-Agent",
                        "DownloadSubDivisions/1 (https://github.com/mihxil/i18n-subdivisions) Java-http-client/21")

                .GET()
                .build()

        def send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        status = send.statusCode()
        lastModified = send.headers().firstValue("Last-modified")
                .map(s ->DateTimeFormatter.RFC_1123_DATE_TIME.parse(s, ZonedDateTime::from).toInstant()).orElse(null)


        if (status == 200) {
            wikiHtml = send.body()
        } else {
            println(wikiUrl.toString() + "-->" + send.statusCode());
            wikiHtml = null;
        }
    } catch (IOException e) {
        println(e.getMessage())
    }
    if (wikiHtml) {
        new File(buildResources, "${it.code}.wiki.html").withWriter("UTF-8", {
            it.write(wikiHtml)
        })
    } else {
        println("Failed to download ${wikiUrl}")
    }
    new File(buildResources, "${it.code}.wiki.url").withWriter("UTF-8", {
        it.write(wikiUrl.toString() + "\t" + status + "\t" + lastModified)
    })
}
