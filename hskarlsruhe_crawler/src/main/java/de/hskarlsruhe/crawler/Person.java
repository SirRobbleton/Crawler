package de.hskarlsruhe.crawler;

/**
 * @author Richard Gottschalk
 */
public class Person {
    String firstName;
    String lastName;
    String room;
    String building;
    String phone;
    String email;
    String consultationHour;
    String academicDegree;
    String imageUrl;

    public boolean isValid() {
        return lastName != null && lastName.length() > 0 && email != null && email.length() > 0;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConsultationHour() {
        return consultationHour;
    }

    public void setConsultationHour(String consultationHour) {
        this.consultationHour = consultationHour;
    }

    public String getAcademicDegree() {
        return academicDegree;
    }

    public void setAcademicDegree(String academicDegree) {
        this.academicDegree = academicDegree;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", building='" + building + '\'' +
                ", room='" + room + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", consultationHour='" + consultationHour + '\'' +
                ", academicDegree='" + academicDegree + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public String toCSV() {
        return "\"" + academicDegree + '\"' +
                ",\"" + firstName + '\"' +
                ",\"" + lastName + '\"' +
                ",\"" + building + '\"' +
                ",\"" + room + '\"' +
                ",\"" + phone + '\"' +
                ",\"" + email + '\"' +
                ",\"" + consultationHour + '\"' +
                ",\"" + imageUrl + '\"';
    }

    public String toSQL(int id, long createdAt) {
        return String.format("INSERT INTO \"person\" (\"id\"," +
                "\"version\"," +
                "\"created_at\"," +
                "\"updated_at\"," +
                "\"academic_degree\"," +
                "\"consultation_hour\"," +
                "\"email\"," +
                "\"first_name\"," +
                "\"last_name\"," +
                "\"phone\"," +
                "\"remark\"," +
                "\"title\"," +
                "\"room_id\"," +
                "\"image_url\"" +
                ") VALUES (" +
                "\"%s\"," +
                "\"%s\"," +
                "%d," +
                "%d," +
                "\"%s\"," +
                "\"%s\"," +
                "\"%s\"," +
                "\"%s\"," +
                "\"%s\"," +
                "\"%s\"," +
                "\"%s\"," +
                "\"%s\"," +
                "%s," +
                "\"%s\");",
                id,
                "1",
                createdAt,
                createdAt,
                academicDegree,
                consultationHour,
                email,
                firstName,
                lastName,
                phone,
                "",
                "",
                String.format("(SELECT id FROM room WHERE name = \"%s\" AND building_id = (SELECT id FROM building WHERE name = \"%s\"))", room, building),
                imageUrl
                );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
        if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
        if (phone != null ? !phone.equals(person.phone) : person.phone != null) return false;
        if (email != null ? !email.equals(person.email) : person.email != null) return false;
        if (academicDegree != null ? !academicDegree.equals(person.academicDegree) : person.academicDegree != null)
            return false;
        return imageUrl != null ? imageUrl.equals(person.imageUrl) : person.imageUrl == null;

    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (academicDegree != null ? academicDegree.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        return result;
    }
}
