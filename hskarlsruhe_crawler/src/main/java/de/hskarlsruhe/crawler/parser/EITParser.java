package de.hskarlsruhe.crawler.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

/**
 * @author Richard Gottschalk
 */
public class EITParser extends Parser {

    protected List<Element> findElements(Document document) {
        return document.getElementsByClass("accordion");
    }
}
