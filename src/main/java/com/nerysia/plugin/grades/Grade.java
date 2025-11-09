package com.nerysia.plugin.grades;

public enum Grade {
    FONDATEUR("Fonda", "§4§l", 7, "Fonda"),
    RESPONSABLE("Responsable", "§c§l", 6, "Resp"),
    ADMINISTRATEUR("Admin", "§c", 5, "Admin"),
    MODERATEUR("Modo", "§9", 4, "Modo"),
    MODERATEUR_TEST("ModoTest", "§b", 3, "ModoTest"),
    STREAMEUR("Streameur", "§d", 2, "Streameur"),
    JOUEUR("Joueur", "§7", 1, "Joueur");

    private final String name;
    private final String prefix;
    private final int power;
    private final String shortName;

    Grade(String name, String prefix, int power, String shortName) {
        this.name = name;
        this.prefix = prefix;
        this.power = power;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getPower() {
        return power;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDisplayName() {
        return prefix + name;
    }

    public String getTabName() {
        switch (this) {
            case FONDATEUR:
                return "Fondateur";
            case ADMINISTRATEUR:
                return "Administrateur";
            case MODERATEUR:
                return "Modérateur";
            case MODERATEUR_TEST:
                return "Modo Test";
            default:
                return name;
        }
    }

    public static Grade getByName(String name) {
        for (Grade grade : values()) {
            if (grade.name().equalsIgnoreCase(name) || grade.getName().equalsIgnoreCase(name)) {
                return grade;
            }
        }
        return null;
    }
}
