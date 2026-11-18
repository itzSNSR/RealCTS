package com.realcts.app;

import java.util.Objects;

final class RestorePolicy {
    static boolean owns(String current, String temporary, String assistant, String originalAssistant) {
        return temporary != null && temporary.equals(current) && Objects.equals(assistant, originalAssistant);
    }
}
