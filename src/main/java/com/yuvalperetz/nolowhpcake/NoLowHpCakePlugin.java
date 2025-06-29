
package com.yuvalperetz.nolowhpcake;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.swing.JOptionPane;


@PluginDescriptor(
        name = "Dwarven rock cake helper",
        description = "Warns before guzzling Dwarven rock cake at low HP and alerts when HP rises above threshold"
)
public class NoLowHpCakePlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private NoLowHpCakeConfig config;

    private int lastHp = -1;

    @Override
    protected void startUp() {
        lastHp = -1;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        String option = event.getMenuOption();
        String target = Text.removeTags(event.getMenuTarget());

        if ("Guzzle".equals(option) && "Dwarven rock cake".equals(target)) {
            int hp = client.getBoostedSkillLevel(Skill.HITPOINTS);
            int threshold = config.hpThreshold();
            if (hp < threshold) {
                int result = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to guzzle the dwarven rock cake? You will die.",
                        "Confirm Guzzle",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (result != JOptionPane.YES_OPTION) {
                    event.consume();
                }
            }
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (event.getSkill() != Skill.HITPOINTS) {
            return;
        }
        int hp = event.getBoostedLevel();
        int threshold = config.hpThreshold();
        boolean alertEnabled = config.alertOnHpAboveThreshold();
        // Alert every time HP increases to >= threshold
        if (alertEnabled && lastHp != -1 && hp > lastHp && hp >= threshold) {
            JOptionPane.showMessageDialog(
                    null,
                    "Your HP is now " + hp + ", which is at or above your threshold (" + threshold + ")!",
                    "HP Alert",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
        lastHp = hp;
    }

    @Provides
    NoLowHpCakeConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(NoLowHpCakeConfig.class);
    }
}


@ConfigGroup("nolowhpcake")
interface NoLowHpCakeConfig extends Config {

    @ConfigItem(
            keyName = "hpThreshold",
            name = "HP Threshold",
            description = "Warn/block Guzzle when HP is below this value",
            position = 0
    )
    default int hpThreshold() {
        return 2;
    }

    @ConfigItem(
            keyName = "alertOnHpAboveThreshold",
            name = "Alert when HP >= threshold",
            description = "Show an alert when HP increases to or above the threshold",
            position = 1
    )
    default boolean alertOnHpAboveThreshold() {
        return true;
    }
}
