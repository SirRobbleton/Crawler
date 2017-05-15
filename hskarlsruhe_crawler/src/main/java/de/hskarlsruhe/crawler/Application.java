package de.hskarlsruhe.crawler;

import java.util.List;

/**
 * @author Richard Gottschalk
 */
public class Application {

    public static void main(String[] args) {
        Crawler crawler = new Crawler();

        List<Person> personList = crawler.parseAll();

        long createdAt = System.currentTimeMillis();

        for (int i = 0; i < personList.size(); i++) {
            Person person = personList.get(i);
            System.out.println(person.toSQL(i + 1, createdAt));
        }

        System.out.println("Persons: " + personList.size());
    }
}
