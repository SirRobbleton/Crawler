package de.hskarlsruhe.crawler.parser;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import de.hskarlsruhe.crawler.Person;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Richard Gottschalk
 */
public abstract class Parser {

    private static String[] ACADEMIC_DEGREES = {
            "Dr\\.(?:[^ ]+| h\\.c\\.)?",
            "Dipl\\.[ \\-]{0,2}[^ ]+",
            "Diplom-[^ ]+",
            "[^ ]+rchitekt[^ ]*",
            "[^ ]+nformatik[^ ]*",
            "h\\. ?c\\.",
            "[a-zA-Z]{2,}\\.",
            "\\(FH\\)",
            "\\(BAW\\)",
            "\\(GHD\\)",
            "B\\. ?[^ ]+",
            "M\\. ?[^ ]+",
            "MBA",
            "Ingénieur diplômé de [^ ]+"
    };

    private static URI BASE_URI = URI.create("https://www.hs-karlsruhe.de/");

    protected Pattern CONSULATION_HOURS_PATTERN = Pattern.compile("Sprechzeiten: ([^\\n<>]*)");
    protected Pattern EMAILS_PATTERN = Pattern.compile("(?i)(\\b[A-Z0-9._%+-]+(?:\\[at\\]|@|\\(at\\))[A-Z0-9.-]+\\.[A-Z]{2,}\\b)");
    protected Pattern PHONE_PATTERN = Pattern.compile("Tel[a-z\\.]*:? (\\+?[0-9\\-/ \\(\\)]+)");
    protected Pattern BUILDING_PATTERN = Pattern.compile("Gebäude:? (\\w+)");
    protected Pattern ROOM_PATTERN = Pattern.compile("(?:Zimmer|Raum|R):? [a-zA-Z\\-]{0,3}(\\d\\w+)");
    private Pattern ACADEMIC_DEGREE_PATTERN;

    public Parser() {
        init();
    }

    private void init() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (String degree : ACADEMIC_DEGREES) {
            if (stringBuilder.length() > 1) stringBuilder.append('|');
            stringBuilder.append(degree);
        }
        stringBuilder.append(")");
        ACADEMIC_DEGREE_PATTERN = Pattern.compile(stringBuilder.toString());
    }

    public final List<Person> parseAllPersons(Document document) {
        List<Person> persons = new ArrayList<>();

        List<Element> elements = findElements(document);
        for (Element element : elements) {
            try {
                Person person = parsePerson(element);
                if (person != null) persons.add(person);
            } catch (IndexOutOfBoundsException e) {}
        }

        return persons;
    }

    protected List<Element> findElements(Document document) {
        return document.getElementsByClass("ce-bodytext");
    }

    private Person parsePerson(Element element) {
        if (element.text().contains("In dieser Spalte finden Sie eine Liste der Lehrbeauftragten")) {
//            avoid lists like:
//            http://www.hs-karlsruhe.de/fakultaeten/informatik-und-wirtschaftsinformatik/personen/lehrbeauftragte
            return null;
        }

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

    protected String parseRoom(Element element) {
        String roomRaw = getRoomRaw(element);
        String[] locationDetails = roomRaw.split(" ");
        int len = locationDetails.length;
        for (int i = 0; i < len; i++) {
            if (locationDetails[i] == "Raum") {
                return locationDetails[i+1];
            }
        }
        return "";
    }

    protected String parseBuilding(Element element) {
        String roomRaw = getRoomRaw(element);
        if (roomRaw.matches("[a-zA-Z]\\d+")) {
            return roomRaw.substring(0, 1);
        } else {
            Matcher matcher = BUILDING_PATTERN.matcher(element.text());

            if (matcher.find()) {
                return matcher.group(1);
            }
            return "";
        }
    }

    private String getRoomRaw(Element element) {
        List<Element> list = element.getElementsByClass("bodytext");

        for (Element el : list){
            if (el.text().contains("Raum")){
                return el.text();
            }
        }

        Matcher matcher = ROOM_PATTERN.matcher(element.text());

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    protected String parseFirstNames(Element element) {
        String fullName = parseFullName(element);
        if (fullName.contains(",")) {
            return fullName.substring(fullName.indexOf(',') + 1).trim();
        } else {
            return fullName.substring(0, fullName.lastIndexOf(' ')).trim();
        }
    }

    protected String parseLastName(Element element) {
        String fullName = parseFullName(element);
        if (fullName.contains(",")) {
            return fullName.substring(0, fullName.lastIndexOf(',')).trim();
        } else {
            return fullName.substring(fullName.lastIndexOf(' ')).trim();
        }
    }

    protected String parseFullName(Element element) {
        String name = parseFullNameWithAcademicDegree(element);

        // remove "(FH)" or "(BAW)" for name parsing
        name = name.replaceAll("\\([^\\)]*\\)", "").trim();
        if (name.endsWith(")")) name = name.substring(0, name.length() - 1).trim();

        // remove academic degree
        boolean replaced;
        do {
            replaced = false;
            Matcher matcher = ACADEMIC_DEGREE_PATTERN.matcher(name);

            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    name = name.replace(matcher.group(i + 1), "");
                    replaced = true;
                }
            }
        } while (replaced);

        while (name.trim().endsWith(",")) {
            name = name.substring(0, name.lastIndexOf(','));
        }

        if (name.matches("\\([^\\)]+\\)$")) {
            name = name.replaceFirst("\\([^\\)]+\\)$", "");
        }

        return name.trim();
    }

    protected String parseFullNameWithAcademicDegree(Element box) {
        return box.getElementsByTag("h4").get(0).text();
    }

    protected String parseEmail(Element box) {

        Matcher matcher = EMAILS_PATTERN.matcher(box.text());
        if (matcher.find()) {
            String plain = matcher.group(1).trim();
            plain = plain.replace("[at]", "@");
            plain = plain.replace("(at)", "@");
            return plain.toLowerCase();
        }
        return "";
    }

    protected String parseConsultationHour(Element box) {
        Matcher matcher = CONSULATION_HOURS_PATTERN.matcher(box.toString());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    protected String parseAcademicDegree(Element element) {
        String name = parseFullNameWithAcademicDegree(element);

        name = name.replaceAll("\\(FH\\)", "--FH--");
        name = name.replaceAll("[\\(\\)]", "");
        name = name.replaceAll("\\-\\-FH\\-\\-", "(FH)");

        StringBuilder stringBuilder = new StringBuilder();

        boolean replaced;
        do {
            replaced = false;
            Matcher matcher = ACADEMIC_DEGREE_PATTERN.matcher(name);

            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    if (stringBuilder.length() > 0) stringBuilder.append(' ');
                    stringBuilder.append(matcher.group(i + 1));
                    name = name.replaceFirst(excapeForRegEx(matcher.group(i + 1)), "");
                    replaced = true;
                }
            }
        } while (replaced);

        return stringBuilder.toString();
    }

    private String excapeForRegEx(String text) {
        text = text.replace("(", "\\(");
        text = text.replace(")", "\\)");
        text = text.replace(".", "\\.");
        return text;
    }

    protected String parseImage(Element element) {
        Elements elements = element.getElementsByTag("img");

        if (elements.size() > 0) {
            Element img = elements.get(0);
            String src = img.attr("src");
            URI uri = URI.create(src);
            if (!uri.isAbsolute()) {
                uri = URI.create(BASE_URI.getHost() + "/" + src);
            }
            return uri.toString();
        }
        return "";
    }

    protected String parsePhone(Element element) {
        Matcher matcher = PHONE_PATTERN.matcher(element.text());

        if (matcher.find()) {
            String group = matcher.group(1);
            return normalizePhone(group);
        }

        return "";
    }

    protected String normalizePhone(String phone) {
        if (phone == null || phone.length() == 0) return "";

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phone, "DE");
            return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            System.err.println("unable to parse to phone number: " + phone);
            return "";
        }
    }

    private boolean isValidPerson(Person person) {
        if (StringUtil.isBlank(person.getLastName())) return false;
        //if (StringUtil.isBlank(person.getEmail())) return false;
//        if (StringUtil.isBlank(person.getBuilding())) return false;
//        if (StringUtil.isBlank(person.getRoom())) return false;
        return true;
    }
}
