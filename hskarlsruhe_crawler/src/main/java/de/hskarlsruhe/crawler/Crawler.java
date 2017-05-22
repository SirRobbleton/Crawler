package de.hskarlsruhe.crawler;

import de.hskarlsruhe.crawler.parser.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Richard Gottschalk
 */
public class Crawler {

    private final String BASE_URL = "https://www.hs-karlsruhe.de/";

    protected Map<String, Parser> BASE_SUB_URLS = new TreeMap<>();

    public Crawler() {
        //BASE_SUB_URLS.put("fk-ab/ueber-uns/personen", new ABParser());
        //BASE_SUB_URLS.put("fk-eit/ueber-uns/personen", new EITParser());
        //BASE_SUB_URLS.put("fk-iwi/ueber-uns/personen", new IWIParser());
        //BASE_SUB_URLS.put("imm/ueber-uns/personen", new IMMParser());
        //BASE_SUB_URLS.put("mmt/ueber-uns/personen", new MMTParser());
        BASE_SUB_URLS.put("w/ueber-uns/personen", new WParser());
    }

    private List<String> done = new ArrayList<>();

    public List<Person> parseAll() {
        List<Person> persons = new ArrayList<>();
        BASE_SUB_URLS.keySet()
                .forEach(key -> parse(key, BASE_SUB_URLS.get(key), persons));
        return persons;
    }

    private void parse(String url, Parser parser, List<Person> target) {
        if (url.startsWith(BASE_URL)) url = url.replaceFirst(BASE_URL, "");

        try {
            List<String> sublists = findSublists(url);
            parseSublist(sublists, parser, target);
        } catch (IOException ignore) {
            ignore.printStackTrace();
        }
    }

    private void parseSublist(List<String> relativeLinks, Parser parser, List<Person> target) throws IOException {
        for (String relativeUrl : relativeLinks) {
            List<String> sublist = findSublists(relativeUrl);

            parsePersons(relativeUrl, parser, target);
            parseSublist(sublist, parser, target);
        }
    }

    private List<String> findSublists(final String relativeUrl) throws IOException {
        if (isDone(relativeUrl)) return new ArrayList<>();
        setDone(relativeUrl);

        Document doc = getDocument(BASE_URL + relativeUrl);

        Elements allLinks = doc.getElementsByTag("a");
        return allLinks.stream()
                .map(element -> element.attr("href"))
                .map(this::normalizeLink)
                .filter(link -> link.contains("person") || link.startsWith(relativeUrl))
                .filter(link -> !link.contains("login"))
                .filter(link -> !link.contains("ruhestand"))
                .filter(link -> !link.contains("drucken"))
                .filter(link -> !link.contains("/lehrbeauf")) // avoid Lehrbeauftragte
                .filter(link -> link.contains("ueber-uns")) // avoid www.hs-karlsruhe.de/fakultaeten/fk-iwi/ueber-uns
                .filter(link -> !link.contains("aktuelles")) // avoid www.hs-karlsruhe.de/fakultaeten/fk-iwi/aktuelles
                .filter(link -> !isDone(link))
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeLink(String link) {
        String normalized;

        if (link.startsWith(BASE_URL)) {
            normalized = link.replaceFirst(BASE_URL, "");
        } else {
            normalized = link;
        }

        normalized = normalized.replace(".html", "");
        normalized = normalized.replaceFirst("\\&cHash\\=[0-9a-f]+", "");
        return normalized;
    }

    private void parsePersons(String url, Parser parser, List<Person> target) throws IOException {
        Document doc = getDocument(BASE_URL + url);

        if (parser != null) {
            parser.parseAllPersons(doc).stream()
                    .filter(person -> !target.contains(person))
                    .forEach(target::add);
        }
    }

    private boolean isDone(String url) {
        return done.contains(url);
    }

    private void setDone(String url) {
        done.add(url);
    }

    private Document getDocument(String url) throws IOException
    {
        String currentFolder = System.getProperty("user.dir");
        String localFileName = url.replace(":", "").replace("?", "_");
        if (!localFileName.endsWith(".html")) localFileName += ".html";
        File bufferedFile = new File(currentFolder + "/_buffer", localFileName);

        if (bufferedFile.exists() && !localFileName.contains("?")) {
            System.out.println("Parsing local " + url);
            return Jsoup.parse(bufferedFile, "UTF-8", "https://www.hs-karlsruhe.de/");
        } else {
            System.out.println("Parsing remote " + url);
            Document document = Jsoup.connect(url).get();

            bufferedFile.getParentFile().mkdirs();

            FileWriter fileWriter = new FileWriter(bufferedFile);
            fileWriter.write(document.toString());
            fileWriter.close();

            try {
                // sleeping to reduce traffic and avoid HTTP 429
                Thread.sleep((long) (Math.random() * 1000 * 20 + 5 * 1000));
            } catch (InterruptedException ignore) {}

            return document;
        }
    }

}
