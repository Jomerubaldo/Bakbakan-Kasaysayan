module com.bakbakan.bakbakankasaysayan {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    exports com.bakbakan.app;
    exports com.bakbakan.screens;
    exports com.bakbakan.entities;
    exports com.bakbakan.combat;
    exports com.bakbakan.ai;
    exports com.bakbakan.events;
    exports com.bakbakan.audio;
    exports com.bakbakan.assets;
    exports com.bakbakan.ui;
    exports com.bakbakan.util;

    opens com.bakbakan.app;
    opens com.bakbakan.screens;
    opens com.bakbakan.entities;
    opens com.bakbakan.combat;
    opens com.bakbakan.ai;
    opens com.bakbakan.events;
    opens com.bakbakan.audio;
    opens com.bakbakan.assets;
    opens com.bakbakan.ui;
    opens com.bakbakan.util;
}
