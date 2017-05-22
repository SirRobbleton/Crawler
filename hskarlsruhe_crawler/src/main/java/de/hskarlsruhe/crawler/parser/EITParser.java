package de.hskarlsruhe.crawler.parser;

import de.hskarlsruhe.crawler.Person;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * @author Richard Gottschalk
 */
public class EITParser extends Parser {

    @Override
    protected List<Element> findElements(Document document) {
        return document.getElementsByClass("accordion");
    }

    @Override
    protected Person parsePerson(Element e) {
        if (e.text().contains("In dieser Spalte finden Sie eine Liste der Lehrbeauftragten")) {
//            avoid lists like:
//            http://www.hs-karlsruhe.de/fakultaeten/informatik-und-wirtschaftsinformatik/personen/lehrbeauftragte
            return null;
        }

        Element element = e;

        Person person = new Person();


        person.setLastName(parseLastName(element));
        person.setFirstName(parseFirstNames(element));
        person.setEmail(parseEmail(element));
        person.setImageUrl(parseImage(element));
        person.setConsultationHour(parseConsultationHour(element));
        person.setAcademicDegree(parseAcademicDegree(element));
        person.setPhone(parsePhone(element));
        person.setBuilding(parseBuilding(element));
        person.setRoom(parseRoom(element));

        if (Parser.isValidPerson(person)) return person;
        return null;
    }

    @Override
    protected String parseFullNameWithAcademicDegree(Element box) {
        return box.getElementsByTag("h2").get(0).text();
    }
}
