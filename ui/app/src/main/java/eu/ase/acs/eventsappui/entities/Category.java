package eu.ase.acs.eventsappui.entities;

import java.io.Serializable;

public enum Category implements Serializable {
    MUSIC(1),
    THEATRE(2),
    FILM(3),
    GAMING(4),
    ARTS(5),
    COMEDY(6),
    SPORTS(7),
    FITNESS(8),
    EDUCATION(9),
    FOOD_DRINK(10),
    COMMUNITY_CHARITY(11),
    EXHIBITIONS(12),
    MISCELLANEOUS(13);
    private int code;
    Category(int code){
        this.code = code;
    }
    public int getCode(){
        return code;
    }
    public static Category fromInt(int i) {
        for (Category category : Category.values()) {
            if (category.getCode() == i) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}