package ru.yolta.customitemmanager.config;

import org.jetbrains.annotations.NotNull;

final class GuideFiles {

    private GuideFiles() {}

    static void saveGuides(@NotNull ConfigManager manager) {
        manager.saveFile("guides/items.md");
        manager.saveFile("guides/placeholders.md");
    }
}
