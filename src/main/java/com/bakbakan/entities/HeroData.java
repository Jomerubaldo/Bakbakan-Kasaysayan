package com.bakbakan.entities;

import java.util.List;

public record HeroData(
        String id,
        String name,
        String weapon,
        String description,
        double speed,
        double damage,
        double range,
        double defense,
        double manaRegen,
        String primaryColor,
        String accentColor) {

    public static final HeroData DUMMY = new HeroData(
            "dummy",
            "Training Dummy",
            "None",
            "A stationary practice opponent.",
            0, 0, 0, 0, 0,
            "#5a6478", "#c0392b");

    public static final List<HeroData> ALL = List.of(
            new HeroData(
                    "rizal",
                    "Jose Rizal",
                    "Book",
                    "A calm reformist who strikes from afar with books and ink.",
                    55, 60, 90, 40, 70,
                    "#2E5E8C", "#F4C542"),
            new HeroData(
                    "bonifacio",
                    "Andres Bonifacio",
                    "Bolo",
                    "A fiery revolutionary who closes distance fast with bolo and flag.",
                    85, 70, 35, 50, 65,
                    "#B03A2E", "#F5B041"),
            new HeroData(
                    "luna",
                    "Antonio Luna",
                    "Sword & Rifle",
                    "A disciplined general with heavy sword strikes and rifle fire.",
                    45, 90, 65, 65, 55,
                    "#3B5BA5", "#C0392B"),
            new HeroData(
                    "mabini",
                    "Apolinario Mabini",
                    "Cane & Quill",
                    "A tactical genius who controls space with cane and quill.",
                    60, 55, 70, 45, 100,
                    "#2E8B57", "#F0E68C"),
            new HeroData(
                    "lapulapu",
                    "Lapulapu",
                    "Kampilan",
                    "A steadfast chieftain who shrugs off blows and rules the shore.",
                    40, 80, 50, 95, 60,
                    "#8B4513", "#F4C542")
    );

    public static HeroData byId(String id) {
        for (HeroData hero : ALL) {
            if (hero.id.equals(id)) {
                return hero;
            }
        }
        return null;
    }
}
