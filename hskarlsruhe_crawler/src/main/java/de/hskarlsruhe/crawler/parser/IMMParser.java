package de.hskarlsruhe.crawler.parser;

import de.hskarlsruhe.crawler.Person;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Richard Gottschalk
 */
public class IMMParser extends Parser {

    @Override
    protected List<Element> findElements(Document document) {
        return document.getElementsByClass("person");
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

        if (isValidPerson(person)) return person;
        return null;
    }

    @Override
    protected String parseFullNameWithAcademicDegree(Element box) {
        return box.getElementsByClass("person__name").get(0).text();
    }

    @Override
    protected String getRoomRaw(Element element) {
        List<Element> list = element.getElementsByClass("person_details");

        for (Element el : list){
            if (el.text().contains("Raum") || el.text().contains("Zimmer")){
                return el.text();
            }
        }

        /*
        Matcher matcher = ROOM_PATTERN.matcher(element.text());

        if (matcher.find()) {
            return matcher.group(1);
        }
        */
        return "";
    }

    @Override
    protected String parseRoom(Element element){
        String roomRaw = getRoomRaw(element);
        Matcher matcher = ROOM_PATTERN.matcher(element.text());

        if (matcher.find()) {
            String roomDetail = matcher.group(0);
            String[] locationDetails = roomDetail.split("\\s+");
            int len = locationDetails.length;
            String room = new String();
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    if (locationDetails[i].equals("Raum:") || locationDetails[i].equals("Zimmer:")) {
                        room = locationDetails[i + 1];
                    }
                }
            }
            return room;
        }
        return "";
    }
}
