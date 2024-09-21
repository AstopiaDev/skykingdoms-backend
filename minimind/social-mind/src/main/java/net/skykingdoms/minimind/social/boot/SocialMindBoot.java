package net.skykingdoms.minimind.social.boot;

import net.skykingdoms.minimind.social.SocialMind;

public class SocialMindBoot {
    public static void main(String[] args) {
        SocialMind.getInstance().bootstrap(args);
    }
}
