package net.tcpshield.tcpshieldapi;

import java.util.Date;

public interface Network {

    int getID();

    String getName();

    boolean isPremium();

    String getProtectedCNAME();

    String getTXTVerification();

    MitigationSettings getMitigationSettings();

    Date getUpdatedAt();

    Date getCreatedAt();

}
